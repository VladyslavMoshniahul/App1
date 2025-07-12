import { renderVoteCreation, renderAvailableVotes } from './vote.js';
import { fetchWithAuth } from './api.js';

const themeToggleBtn = document.getElementById('toggleThemeButton');
const isDark = localStorage.getItem('theme') === 'dark';



document.addEventListener("DOMContentLoaded", function () {
    const tabUsers = document.getElementById("tab-users");
    const tabCreate = document.getElementById("tab-create");
    const tabProfile = document.getElementById("tab-profile");
    const usersPage = document.getElementById("users-page");
    const createPage = document.getElementById("create-page");
    const profileSection = document.getElementById("profile-section");

    function showPage(page) {
        usersPage.classList.remove("active");
        createPage.classList.remove("active");
        profileSection.classList.remove("active");
        tabUsers.classList.remove("active");
        tabCreate.classList.remove("active");
        tabProfile.classList.remove("active");
        if (page === "users") {
            usersPage.classList.add("active");
            tabUsers.classList.add("active");
        } else if (page === "create") {
            createPage.classList.add("active");
            tabCreate.classList.add("active");
        } else if (page === "profile") {
            profileSection.classList.add("active");
            tabProfile.classList.add("active");
        }
    }

    if (tabUsers && tabCreate && tabProfile && usersPage && createPage && profileSection) {
        tabUsers.addEventListener("click", () => showPage("users"));
        tabCreate.addEventListener("click", () => showPage("create"));
        tabProfile.addEventListener("click", () => showPage("profile"));
    }
});

document.addEventListener("DOMContentLoaded", () => {

    const logoutBtn = document.getElementById("logoutButton");
    if (logoutBtn) {
        logoutBtn.addEventListener("click", () => {
            window.location.href = "login.html";
        });
    }


    loadStats();
    loadUsers();
    loadProfile();

    const goBackBtn = document.getElementById("goBackToMainButton");
    if (goBackBtn) {
        goBackBtn.addEventListener("click", () => {
            if (document.referrer) {
                window.location.href = document.referrer;
            } else {
                window.location.href = "login.html";
            }
        });
    }

    const editProfileForm = document.getElementById("editProfileForm");
    if (editProfileForm) {
        editProfileForm.addEventListener("submit", updateProfile);
    }


    const createUserBtn = document.getElementById("create-user-button");
    if (createUserBtn) {
        createUserBtn.addEventListener("click", async () => {
            const first = document.getElementById("new-user-first").value.trim();
            const last = document.getElementById("new-user-last").value.trim();
            const email = document.getElementById("new-user-email").value.trim();
            const pass = document.getElementById("new-user-pass").value;
            const aboutMe = document.getElementById("new-user-aboutMe").value.trim();
            const dateOfBirth = document.getElementById("new-user-dateOfBirth").value;
            const role = document.getElementById("new-user-role").value;
            const schoolId = document.getElementById("school-select")?.value;
            const classId = document.getElementById("class-select")?.value;

            if (!first || !last || !email || !pass || !role || !aboutMe || !dateOfBirth || !schoolId) {
                alert("Заповніть всі поля та оберіть школу.");
                return;
            }

            try {
                const payload = {
                    firstName: first,
                    lastName: last,
                    email,
                    password: pass,
                    aboutMe,
                    dateOfBirth,
                    role,
                    schoolId: Number(schoolId)
                };
                if (classId) payload.classId = Number(classId);

                const res = await fetchWithAuth("/api/users", {
                    method: "POST",
                    body: JSON.stringify(payload)
                });
                if (!res.ok) throw new Error(res.status);
                alert("Користувача створено!");
                loadUsers();
            } catch (e) {
                console.error("Помилка створення користувача:", e);
                alert("Не вдалося створити користувача.");
            }
        });
    }


    const editUserForm = document.getElementById("edit-user-form");
    if (editUserForm) {
        editUserForm.addEventListener("submit", async (e) => {
            e.preventDefault();

            const id = document.getElementById("edit-user-id").value;
            const firstName = document.getElementById("edit-firstName").value.trim();
            const lastName = document.getElementById("edit-lastName").value.trim();
            const aboutMe = document.getElementById("edit-aboutMe").value.trim();
            const dateOfBirth = document.getElementById("edit-dateOfBirth").value;

            if (!firstName || !lastName || !aboutMe || !dateOfBirth) {
                alert("Заповніть всі поля.");
                return;
            }

            try {
                const res = await fetchWithAuth(`/api/users/${id}`, {
                    method: "PUT",
                    body: JSON.stringify({ firstName, lastName, aboutMe, dateOfBirth })
                });

                if (!res.ok) throw new Error(res.status);
                alert("Профіль оновлено!");
                document.getElementById("edit-user-section").style.display = "none";
                loadUsers();
            } catch (e) {
                console.error("Помилка оновлення користувача:", e);
                alert("Не вдалося оновити профіль.");
            }
        });
    }

    // Add this block to initialize school/class selects and stats
    initSchoolClassSelectors();
});

async function initSchoolClassSelectors() {
    const schoolSel = document.getElementById("school-select");
    const classSel = document.getElementById("class-select");
    if (!schoolSel || !classSel) return;

    // Load schools
    try {
        const resS = await fetchWithAuth("/api/schools");
        if (!resS.ok) throw new Error(resS.status);
        const schools = await resS.json();
        schoolSel.innerHTML = `<option value=''>Оберіть школу</option>`;
        schools.forEach(s => {
            schoolSel.innerHTML += `<option value="${s.id}">${s.name}</option>`;
        });
    } catch (e) {
        schoolSel.innerHTML = `<option value=''>Не вдалося завантажити школи</option>`;
        classSel.innerHTML = `<option value=''>---</option>`;
        return;
    }

    // When school changes, load classes and update stats
    schoolSel.onchange = async () => {
        const schoolId = schoolSel.value;
        if (!schoolId) {
            classSel.innerHTML = `<option value=''>Оберіть школу</option>`;
            await loadStats();
            return;
        }
        classSel.innerHTML = `<option>Завантаження...</option>`;
        try {
            const resC = await fetchWithAuth(`/api/classes?schoolId=${schoolId}`);
            if (!resC.ok) throw new Error(resC.status);
            const classes = await resC.json();
            classSel.innerHTML = `<option value=''>Усі класи</option>`;
            classes.forEach(c => {
                classSel.innerHTML += `<option value="${c.id}">${c.name}</option>`;
            });
        } catch (e) {
            classSel.innerHTML = `<option value=''>Не вдалося завантажити класи</option>`;
        }
        await loadStats();
    };

    // When class changes, update stats
    classSel.onchange = async () => {
        await loadStats();
    };

    // Initial stats
    await loadStats();
}

async function loadUsers() {
    try {
        const res = await fetchWithAuth("/api/loadUsers");
        if (!res.ok) throw new Error(res.status);
        const users = await res.json();

        const userList = document.getElementById("user-list");
        if (!userList) return;

        userList.innerHTML = "";
        users.forEach(user => {
            const li = document.createElement("li");
            li.textContent = `${user.firstName} ${user.lastName} (${user.email})`;

            const editBtn = document.createElement("button");
            editBtn.textContent = "Змінити профіль";
            editBtn.style.marginLeft = "10px";
            editBtn.addEventListener("click", () => openEditForm(user));

            li.appendChild(editBtn);
            userList.appendChild(li);
        });
    } catch (e) {
        console.error("Помилка завантаження користувачів:", e);
    }
}

function openEditForm(user) {
    document.getElementById("edit-user-section").style.display = "block";
    document.getElementById("edit-user-id").value = user.id;
    document.getElementById("edit-firstName").value = user.firstName;
    document.getElementById("edit-lastName").value = user.lastName;
    document.getElementById("edit-aboutMe").value = user.aboutMe;
    document.getElementById("edit-dateOfBirth").value = user.dateOfBirth;
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

renderAvailableVotes('available-votes-container');
renderVoteCreation('vote-create-container');


// ===== Переклад для директора =====
const $ = id => document.getElementById(id);

const translations = {
  ua: {
    langButton: "🌐 English",
    tabs: {
      users: "Користувачі",
      create: "Додавання/Статистика",
      profile: "Профіль"
    },
    profile: {
      title: "Інформація про профіль",
      update: "Оновити профіль",
      name: "Ім'я:",
      surname: "Прізвище:",
      birth: "Дата народження:",
      about: "Про мене:",
      email: "Email:",
      role: "Роль:",
      newPass: "Новий пароль:",
      confirmPass: "Підтвердження пароля:",
      saveBtn: "Оновити профіль"
    }
  },
  en: {
    langButton: "🌐 Українська",
    tabs: {
      users: "Users",
      create: "Add/Statistics",
      profile: "Profile"
    },
    profile: {
      title: "Profile Information",
      update: "Update Profile",
      name: "Name:",
      surname: "Surname:",
      birth: "Date of Birth:",
      about: "About Me:",
      email: "Email:",
      role: "Role:",
      newPass: "New Password:",
      confirmPass: "Confirm Password:",
      saveBtn: "Update Profile"
    }
  }
};

let currentLang = localStorage.getItem("lang") || "ua";

function applyLanguage(lang) {
  const t = translations[lang];

  if ($("toggleLangBtn")) $("toggleLangBtn").textContent = t.langButton;
  if ($("tab-users")) $("tab-users").textContent = t.tabs.users;
  if ($("tab-create")) $("tab-create").textContent = t.tabs.create;
  if ($("tab-profile")) $("tab-profile").textContent = t.tabs.profile;

  // Профіль
  if ($("profile-firstName")) $("profile-firstName").parentElement.childNodes[0].textContent = t.profile.name;
  if ($("profile-lastName")) $("profile-lastName").parentElement.childNodes[0].textContent = t.profile.surname;
  if ($("profile-dateOfBirth")) $("profile-dateOfBirth").parentElement.childNodes[0].textContent = t.profile.birth;
  if ($("profile-aboutMe")) $("profile-aboutMe").parentElement.childNodes[0].textContent = t.profile.about;
  if ($("profile-email")) $("profile-email").parentElement.childNodes[0].textContent = t.profile.email;
  if ($("profile-role")) $("profile-role").parentElement.childNodes[0].textContent = t.profile.role;

  const form = $("editProfileForm");
  if (form) {
    form.querySelector("label[for='edit-firstName']").textContent = t.profile.name;
    form.querySelector("label[for='edit-lastName']").textContent = t.profile.surname;
    form.querySelector("label[for='edit-aboutMe']").textContent = t.profile.about;
    form.querySelector("label[for='edit-dateOfBirth']").textContent = t.profile.birth;
    form.querySelector("label[for='edit-email']").textContent = t.profile.email;
    form.querySelector("label[for='edit-password']").textContent = t.profile.newPass;
    form.querySelector("label[for='confirm-password']").textContent = t.profile.confirmPass;
    form.querySelector("button[type='submit']").textContent = t.profile.saveBtn;
  }

  const updateTitle = form?.parentElement?.querySelector("h2");
  if (updateTitle) updateTitle.textContent = t.profile.update;

  const infoTitle = document.querySelector("#profile-info h2");
  if (infoTitle) infoTitle.textContent = t.profile.title;
}

function toggleLanguage() {
  currentLang = currentLang === "ua" ? "en" : "ua";
  localStorage.setItem("lang", currentLang);
  applyLanguage(currentLang);
}

document.addEventListener("DOMContentLoaded", () => {
  if (!$("toggleLangBtn")) {
    const btn = document.createElement("button");
    btn.id = "toggleLangBtn";
    btn.className = "lang-toggle-button";
    btn.textContent = translations[currentLang].langButton;
    btn.style.marginLeft = "10px";
    btn.addEventListener("click", toggleLanguage);

    const container = document.querySelector("header") || document.body;
    container.appendChild(btn);
  }

  applyLanguage(currentLang);
});

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