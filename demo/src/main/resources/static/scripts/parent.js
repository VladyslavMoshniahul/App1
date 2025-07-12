import { renderVoteCreation, renderAvailableVotes } from './vote.js';

let schoolId = null, classId = null;
let currentMonth, currentYear;

async function fetchWithAuth(url, opts = {}) {
    const token = localStorage.getItem("jwtToken");
    opts.headers = {
        ...(opts.headers || {}),
        "Authorization": `Bearer ${token}`
    };
    return fetch(url, opts);
}

// Tab/page switching logic
document.addEventListener("DOMContentLoaded", function () {
    const tabMain = document.getElementById("tab-main");
    const tabProfile = document.getElementById("tab-profile");
    const mainPage = document.getElementById("main-page");
    const profilePage = document.getElementById("profile-page");

    function showPage(page) {
        mainPage.classList.remove("active");
        profilePage.classList.remove("active");
        tabMain.classList.remove("active");
        tabProfile.classList.remove("active");
        if (page === "main") {
            mainPage.classList.add("active");
            tabMain.classList.add("active");
        } else {
            profilePage.classList.add("active");
            tabProfile.classList.add("active");
        }
    }

    if (tabMain && tabProfile && mainPage && profilePage) {
        tabMain.addEventListener("click", () => showPage("main"));
        tabProfile.addEventListener("click", () => showPage("profile"));
    }
});

document.addEventListener("DOMContentLoaded", () => {
    // Logout button
    const logoutBtn = document.getElementById("logoutButton");
    if (logoutBtn) {
        logoutBtn.addEventListener("click", () => {
            localStorage.removeItem("jwtToken");
            window.location.href = "login.html";
        });
    }

    // Go back button
    document.getElementById("goBackToMainButton")?.addEventListener("click", () => {
        if (document.referrer) {
            window.location.href = document.referrer;
        } else {
            window.location.href = "login.html";
        }
    });

    // Profile form submit
    document.getElementById('editProfileForm')?.addEventListener('submit', updateProfile);

    // Initialize selectors, invitations, calendar, and profile
    initSelectors();
    loadProfile();
    
    // Initialize voting components
    renderAvailableVotes('available-votes-container');
    renderVoteCreation('vote-create-container');
    
    // Initialize theme toggle
    initThemeToggle();
    
    // Initialize language toggle
    initLanguageToggle();
});

async function initSelectors() {
    const schoolSel = document.getElementById("school-select");
    const classSel = document.getElementById("class-select");
    if (schoolSel && classSel) {
        const resS = await fetchWithAuth("/api/schools");
        const schools = await resS.json();
        schoolSel.innerHTML = `<option value=''>Оберіть школу</option>`;
        schools.forEach(s => {
            schoolSel.innerHTML += `<option value="${s.id}">${s.name}</option>`;
        });

        schoolSel.onchange = async () => {
            schoolId = schoolSel.value || null;
            classSel.innerHTML = `<option>Завантаження...</option>`;
            if (!schoolId) {
                classSel.innerHTML = `<option value=''>Усі класи</option>`;
                classId = null;
                await loadInvitations();
                await updateCalendar();
                return;
            }
            const resC = await fetchWithAuth(`/api/classes?schoolId=${schoolId}`);
            const classes = await resC.json();
            classSel.innerHTML = `<option value=''>Усі класи</option>`;
            classes.forEach(c => {
                classSel.innerHTML += `<option value="${c.id}">${c.name}</option>`;
            });
            classId = null;
            await loadInvitations();
            await updateCalendar();
        };

        classSel.onchange = async () => {
            classId = classSel.value || null;
            await loadInvitations();
            await updateCalendar();
        };
    }

    await loadInvitations();
    await initCalendar();
}

async function loadInvitations() {
    const list = document.getElementById("invitations-list");
    if (!list) return;
    list.innerHTML = "";
    let events = [];
    try {
        // Filter by school/class if available
        const qs = new URLSearchParams();
        if (schoolId) qs.set("schoolId", schoolId);
        if (classId) qs.set("classId", classId);
        const res = await fetchWithAuth("/api/getEvents?" + qs);
        events = await res.json();
    } catch (e) {
        list.innerHTML = "<li>Не вдалося завантажити події</li>";
        return;
    }
    
    const filtered = events.filter(e =>
        e.event_type === "PARENTS_MEETING" ||
        (e.event_type && e.event_type.name === "PARENTS_MEETING") ||
        e.eventType === "PARENTS_MEETING"
    );
    
    if (filtered.length === 0) {
        list.innerHTML = "<li>Немає запрошень</li>";
        return;
    }
    
    filtered
        .sort((a, b) => new Date(a.start_event) - new Date(b.start_event))
        .forEach(e => {
            const dt = new Date(e.start_event);
            const li = document.createElement("li");
            li.textContent = `${e.title} — ${dt.toLocaleDateString("uk")}, ${dt.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`;
            list.appendChild(li);
        });
}

async function initCalendar() {
    const now = new Date();
    if (typeof currentMonth !== "number" || typeof currentYear !== "number") {
        currentMonth = now.getMonth();
        currentYear = now.getFullYear();
    }

    const prevBtn = document.getElementById("prev-month");
    const nextBtn = document.getElementById("next-month");
    if (prevBtn) prevBtn.onclick = () => changeMonth(-1);
    if (nextBtn) nextBtn.onclick = () => changeMonth(1);

    // Ensure calendar table and section are visible
    const calendarTable = document.getElementById("calendar-table");
    const calendarSection = document.getElementById("calendar-section");
    if (calendarTable) calendarTable.style.display = "table";
    if (calendarSection) calendarSection.style.display = "";

    await updateCalendar();
}

function changeMonth(delta) {
    currentMonth += delta;
    if (currentMonth < 0) {
        currentMonth = 11;
        currentYear--;
    } else if (currentMonth > 11) {
        currentMonth = 0;
        currentYear++;
    }
    updateCalendar();
}

function showEventModal(event) {
    let modal = document.getElementById("event-modal");
    if (!modal) {
        modal = document.createElement("div");
        modal.id = "event-modal";
        document.body.appendChild(modal);
    }
    
    modal.style.position = "fixed";
    modal.style.top = "0";
    modal.style.left = "0";
    modal.style.width = "100vw";
    modal.style.height = "100vh";
    modal.style.background = "rgba(0,0,0,0.5)";
    modal.style.display = "flex";
    modal.style.alignItems = "center";
    modal.style.justifyContent = "center";
    modal.style.zIndex = "9999";
    modal.innerHTML = `
        <div id="event-modal-content" style="background:#fff;color:#222;padding:24px 32px;border-radius:10px;min-width:320px;max-width:90vw;box-shadow:0 2px 16px rgba(0,0,0,0.25);position:relative;">
            <button id="event-modal-close" style="position:absolute;top:8px;right:12px;font-size:1.3em;background:none;border:none;color:#888;cursor:pointer;">×</button>
            <div id="event-modal-body"></div>
        </div>
    `;
    
    const body = modal.querySelector("#event-modal-body");
    body.innerHTML = `
        <h2 style="color:#ff4c4c;">${event.title}</h2>
        <div><b>Дата:</b> ${event.start_event ? new Date(event.start_event).toLocaleString("uk-UA") : "-"}</div>
        ${event.location_or_link ? `<div><b>Місце/посилання:</b> ${event.location_or_link}</div>` : ""}
        ${event.content ? `<div><b>Опис:</b> ${event.content}</div>` : ""}
        ${event.event_type ? `<div><b>Тип:</b> ${event.event_type.name || event.event_type}</div>` : ""}
        ${event.duration ? `<div><b>Тривалість:</b> ${event.duration} хв</div>` : ""}
    `;
    
    modal.querySelector("#event-modal-close").onclick = () => { modal.style.display = "none"; };
    modal.onclick = (e) => { if (e.target === modal) modal.style.display = "none"; };
    modal.style.display = "flex";
}

async function updateCalendar() {
    const qs = new URLSearchParams();
    if (schoolId) qs.set("schoolId", schoolId);
    if (classId) qs.set("classId", classId);
    let events = [];
    try {
        const res = await fetchWithAuth("/api/getEvents?" + qs);
        events = await res.json();
    } catch (e) {
        events = [];
    }

    const eventsByDay = {};
    events.forEach(ev => {
        if (!ev.start_event) return;
        const dateStr = ev.start_event.slice(0, 10);
        eventsByDay[dateStr] = eventsByDay[dateStr] || [];
        eventsByDay[dateStr].push(ev);
    });

    const mm = document.getElementById("month-name");
    const body = document.getElementById("calendar-body");
    if (mm) mm.style.display = "";
    if (body) body.style.display = "";
    if (!mm || !body) return;

    body.innerHTML = "";
    mm.textContent = new Intl.DateTimeFormat("uk", { month: "long", year: "numeric" })
        .format(new Date(currentYear, currentMonth));

    let fd = new Date(currentYear, currentMonth, 1).getDay();
    fd = fd === 0 ? 6 : fd - 1;
    const daysInMonth = new Date(currentYear, currentMonth + 1, 0).getDate();
    let d = 1;
    
    for (let r = 0; r < 6; r++) {
        const tr = document.createElement("tr");
        for (let c = 0; c < 7; c++) {
            const td = document.createElement("td");
            if ((r === 0 && c < fd) || d > daysInMonth) {
                td.innerHTML = "&nbsp;";
            } else {
                td.textContent = d;
                const key = `${currentYear}-${String(currentMonth + 1).padStart(2, "0")}-${String(d).padStart(2, "0")}`;
                
                (eventsByDay[key] || []).forEach(ev => {
                    const sp = document.createElement("span");
                    sp.classList.add("event");
                    sp.textContent = ev.title;
                    if (ev.id) sp.setAttribute("data-event-id", ev.id);
                    sp.style.cursor = "pointer";
                    sp.onclick = (e) => {
                        e.stopPropagation();
                        showEventModal(ev);
                    };
                    td.appendChild(document.createElement("br"));
                    td.appendChild(sp);
                });
                d++;
            }
            tr.appendChild(td);
        }
        body.appendChild(tr);
    }
}

async function loadProfile() {
    try {
        const res = await fetchWithAuth("/api/me");
        if (!res.ok) throw new Error(res.status);
        const user = await res.json();

        document.getElementById("profile-firstName").textContent = user.firstName || "-";
        document.getElementById("profile-lastName").textContent = user.lastName || "-";
        document.getElementById("profile-aboutMe").textContent = user.aboutMe || "-";
        document.getElementById("profile-dateOfBirth").textContent = user.dateOfBirth || "-";
        document.getElementById("profile-email").textContent = user.email || "-";
        document.getElementById("profile-role").textContent = user.role || "-";

        document.getElementById("edit-firstName").value = user.firstName || "";
        document.getElementById("edit-lastName").value = user.lastName || "";
        document.getElementById("edit-aboutMe").value = user.aboutMe || "";
        document.getElementById("edit-dateOfBirth").value = user.dateOfBirth || "";
        document.getElementById("edit-email").value = user.email || "";
    } catch (e) {
        console.error("Помилка завантаження профілю", e);
        alert("Не вдалося завантажити профіль. Спробуйте ще раз.");
    }
}

async function updateProfile(event) {
    event.preventDefault();
    const form = event.target;
    const formData = new FormData(form);
    const data = Object.fromEntries(formData);

    if (data.password !== data.confirmPassword) {
        alert("Паролі не збігаються!");
        return;
    }

    delete data.confirmPassword;
    if (!data.password) delete data.password;

    Object.keys(data).forEach(key => {
        if (data[key] === "") delete data[key];
    });

    try {
        const res = await fetchWithAuth("/api/me", {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(data),
        });
        if (!res.ok) throw new Error(res.status);
        alert("Профіль успішно оновлено!");
        loadProfile();
    } catch (e) {
        console.error("Помилка оновлення профілю", e);
        alert("Не вдалося оновити профіль.");
    }
}

// Language toggle functionality
const translations = {
    ua: {
        langButton: "🌐 English",
        tabs: {
            main: "Головна інформація",
            profile: "Інформація про мене"
        },
        calendar: {
            title: "Календар подій"
        },
        votes: {
            title: "Голосування"
        },
        profile: {
            title: "Про мене",
            updateTitle: "Оновити профіль",
            name: "Ім'я:",
            surname: "Прізвище:",
            birth: "Дата народження:",
            about: "Про мене:",
            email: "Email:",
            role: "Роль:",
            newPass: "Новий пароль:",
            confirmPass: "Підтвердження пароля:",
            updateBtn: "Оновити профіль"
        }
    },
    en: {
        langButton: "🌐 Українська",
        tabs: {
            main: "Main Info",
            profile: "About Me"
        },
        calendar: {
            title: "Event Calendar"
        },
        votes: {
            title: "Voting"
        },
        profile: {
            title: "About Me",
            updateTitle: "Update Profile",
            name: "Name:",
            surname: "Surname:",
            birth: "Date of Birth:",
            about: "About Me:",
            email: "Email:",
            role: "Role:",
            newPass: "New Password:",
            confirmPass: "Confirm Password:",
            updateBtn: "Update Profile"
        }
    }
};

let currentLang = localStorage.getItem("lang") || "ua";

function applyLanguage(lang) {
    const t = translations[lang];

    // Language button
    if ($("toggleLangBtn")) $("toggleLangBtn").textContent = t.langButton;

    // Tabs
    if ($("tab-main")) $("tab-main").textContent = t.tabs.main;
    if ($("tab-profile")) $("tab-profile").textContent = t.tabs.profile;

    // Calendar
    const calendarHeader = document.querySelector("#calendar-section h2");
    if (calendarHeader) calendarHeader.textContent = t.calendar.title;

    // Voting
    const voteHeader = document.querySelector(".info-card h2");
    if (voteHeader) voteHeader.textContent = t.votes.title;

    // Profile
    const prof = t.profile;
    if ($("profile-firstName")) $("profile-firstName").parentElement.childNodes[0].textContent = prof.name;
    if ($("profile-lastName")) $("profile-lastName").parentElement.childNodes[0].textContent = prof.surname;
    if ($("profile-dateOfBirth")) $("profile-dateOfBirth").parentElement.childNodes[0].textContent = prof.birth;
    if ($("profile-aboutMe")) $("profile-aboutMe").parentElement.childNodes[0].textContent = prof.about;
    if ($("profile-email")) $("profile-email").parentElement.childNodes[0].textContent = prof.email;
    if ($("profile-role")) $("profile-role").parentElement.childNodes[0].textContent = prof.role;

    const form = $("editProfileForm");
    if (form) {
        form.querySelector("label[for='edit-firstName']").textContent = prof.name;
        form.querySelector("label[for='edit-lastName']").textContent = prof.surname;
        form.querySelector("label[for='edit-aboutMe']").textContent = prof.about;
        form.querySelector("label[for='edit-dateOfBirth']").textContent = prof.birth;
        form.querySelector("label[for='edit-email']").textContent = prof.email;
        form.querySelector("label[for='edit-password']").textContent = prof.newPass;
        form.querySelector("label[for='confirm-password']").textContent = prof.confirmPass;
        form.querySelector("button[type='submit']").textContent = prof.updateBtn;
    }

    const sectionTitle = document.querySelector("#profile-page h2");
    if (sectionTitle) sectionTitle.textContent = prof.title;

    const updateTitle = document.querySelector("#update-profile-info-section h2");
    if (updateTitle) updateTitle.textContent = prof.updateTitle;
}

function toggleLanguage() {
    currentLang = currentLang === "ua" ? "en" : "ua";
    localStorage.setItem("lang", currentLang);
    applyLanguage(currentLang);
}

function initLanguageToggle() {
    if (!document.getElementById("toggleLangBtn")) {
        const btn = document.createElement("button");
        btn.id = "toggleLangBtn";
        btn.className = "lang-toggle-button";
        btn.textContent = translations[currentLang].langButton;
        btn.style.marginLeft = "10px";
        btn.addEventListener("click", toggleLanguage);

        const container =
            document.querySelector(".header-buttons") ||
            document.querySelector("header");
        if (container) container.appendChild(btn);
    }

    applyLanguage(currentLang);
}

// Theme toggle functionality
function initThemeToggle() {
    const toggleBtn = document.getElementById('themeToggle');
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;

    if (localStorage.getItem('theme') === 'dark' || (!localStorage.getItem('theme') && prefersDark)) {
        document.body.classList.add('dark-theme');
        toggleBtn.textContent = '☀️';
    }

    toggleBtn.addEventListener('click', () => {
        document.body.classList.toggle('dark-theme');
        const isDark = document.body.classList.contains('dark-theme');
        toggleBtn.textContent = isDark ? '☀️' : '🌙';
        localStorage.setItem('theme', isDark ? 'dark' : 'light');
    });
}

// Helper function
function $(id) {
    return document.getElementById(id);
}