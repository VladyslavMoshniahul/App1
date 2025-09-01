import { fetchWithAuth } from "./api.js";
const tabButtons = document.querySelectorAll(".nav-tabs button");
const sections = document.querySelectorAll(".page-section");

tabButtons.forEach((btn) => {
  btn.addEventListener("click", () => {
    tabButtons.forEach((b) => b.classList.remove("active"));
    sections.forEach((s) => s.classList.remove("active"));

    btn.classList.add("active");

    if (btn.id === "tab-profile") {
      document.getElementById("profile-section").classList.add("active");
    } else if (btn.id === "tab-statistic") {
      document.getElementById("statistics-section").classList.add("active");
    } else if (btn.id === "tab-creating") {
      document.getElementById("creating-page").classList.add("active");
    } else if (btn.id === "tab-petitions") {
      document.getElementById("petitions-section").classList.add("active");
    } else if (btn.id == "tab-events") {
      document.getElementById("events-section").classList.add("active");
    } else if (btn.id === "tab-votes") {
      document.getElementById("votes-section").classList.add("active");
    } else if (btn.id === "tab-invitation") {
      document.getElementById("invitation-section").classList.add("active");
    }

  });
});

const themeToggle = document.getElementById("themeToggle");
const body = document.body;

function applyTheme(theme) {
  if (theme === "dark") {
    body.classList.add("dark-theme");
    themeToggle.textContent = "☀️";
  } else {
    body.classList.remove("dark-theme");
    themeToggle.textContent = "🌙";
  }
}

themeToggle.addEventListener("click", () => {
  const isDark = body.classList.contains("dark-theme");
  const newTheme = isDark ? "light" : "dark";
  applyTheme(newTheme);
  localStorage.setItem("theme", newTheme);
});

document.addEventListener("DOMContentLoaded", () => {
  const savedTheme = localStorage.getItem("theme") || "light";
  applyTheme(savedTheme);
});

const logoutButton = document.getElementById("logoutButton");
logoutButton.addEventListener("click", async () => {
  try {
    const response = await fetch('/api/logout', {
      method: 'POST',
      credentials: 'include'
    });

    if (response.ok) {
      localStorage.clear();
      toastr.success("Ви успішно вийшли з акаунту.");
      window.location.href = "/login.html";
    } else {
      const errorData = await response.json().catch(() => ({ error: 'Невідома помилка виходу' }));
      toastr.error(`Помилка виходу: ${errorData.error}`);
      toastr.error('Logout failed:', response.status, errorData);
    }
  } catch (error) {
    toastr.error("Не вдалося відправити запит на вихід:", error);
    toastr.error("Помилка мережі при виході. Спробуйте ще раз.");
  }
});

document.getElementById("openButton").addEventListener("click", () => {
  loadProfileForEdit();
  document.getElementById("updateProfile").style.display = "flex";
});

document.getElementById("closeButton").addEventListener("click", () => {
  document.getElementById("updateProfile").style.display = "none";
});

async function loadProfileForEdit() {
  try {
    const response = await fetchWithAuth("/api/user/myProfile");
    if (!response.ok) {
      throw new Error("Не вдалося отримати профіль");
    }
    const user = await response.json();

    document.getElementById("edit-firstName").value = user.firstName || "";
    document.getElementById("edit-lastName").value = user.lastName || "";
    document.getElementById("edit-aboutMe").value = user.aboutMe || "";
    document.getElementById("edit-dateOfBirth").value = user.dateOfBirth || "";
    document.getElementById("edit-email").value = user.email || "";
  } catch (error) {
    console.error("Помилка при завантаженні профілю для редагування:", error);
    toastr.error("Не вдалося завантажити дані профілю.");
  }
}

document
  .getElementById("editProfileForm")
  .addEventListener("submit", async (e) => {
    e.preventDefault();

    const password = document.getElementById("edit-password").value.trim();
    const confirmPassword = document
      .getElementById("confirm-password")
      .value.trim();
    const firstName = document.getElementById("edit-firstName").value.trim();
    const lastName = document.getElementById("edit-lastName").value.trim();
    const aboutMe = document.getElementById("edit-aboutMe").value.trim();
    const dateOfBirth = document
      .getElementById("edit-dateOfBirth")
      .value.trim();
    const email = document.getElementById("edit-email").value.trim();
    if (password && password !== confirmPassword) {
      toastr.error("Паролі не співпадають.");
      return;
    }

    try {
      const res = await fetchWithAuth(`/api/user/me`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          firstName,
          lastName,
          aboutMe,
          dateOfBirth,
          email,
          password,
        }),
      });

      if (!res.ok) throw new Error(res.status);
      toastr.success("Профіль успішно оновлено.");
      document.getElementById("updateProfile").style.display = "none";
      document.getElementById("editProfileForm").reset();
    } catch (error) {
      toastr.error("Помилка при оновленні профілю. Спробуйте ще раз.");
    }
  });

document
  .getElementById("create-class-form")
  .addEventListener("submit", async (e) => {
    e.preventDefault();

    const response = await fetchWithAuth("/api/user/myProfile");
    const user = await response.json();
    const schoolName = user.schoolName || "";

    const className = document.getElementById("class-name").value.trim();

    if (!className) {
      toastr.error("Будь ласка, введіть назву класу.");
      return;
    }

    fetchWithAuth("/api/school/create-class", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ schoolName, className }),
    })
      .then((res) => {
        if (!res.ok) throw new Error();
        toastr.success(
          `Клас ${className} у школі ${schoolName} успішно створено.`
        );
        document.getElementById("create-class-form").reset();
      })
      .catch((e) => toastr.error("Не вдалося створити клас." + e.message));
  });

document
  .getElementById("create-user-form")
  .addEventListener("submit", async (e) => {
    e.preventDefault();

    const response = await fetchWithAuth("/api/user/myProfile");
    const user = await response.json();
    const schoolName = user.schoolName || "";

    const firstName = document.getElementById("user-first-name").value.trim();
    const lastName = document.getElementById("user-last-name").value.trim();
    const email = document.getElementById("user-email").value.trim();
    const password = document.getElementById("user-password").value.trim();
    const className = document.getElementById("user-class").value.trim();
    const role = document.getElementById("user-role").value.trim();
    const dateOfBirth = document
      .getElementById("user-dateOfBirth")
      .value.trim();

    if (!firstName) {
      toastr.error("Будь ласка, введіть ім'я.");
      return;
    }
    if (!lastName) {
      toastr.error("Будь ласка, введіть прізвище.");
      return;
    }
    if (!email) {
      toastr.error("Будь ласка, введіть email.");
      return;
    }
    if (!password) {
      toastr.error("Будь ласка, введіть пароль.");
      return;
    }
    if (!role) {
      toastr.error("Будь ласка, оберіть роль для користувача.");
      return;
    }
    if (!dateOfBirth) {
      toastr.error("Будь ласка, введіть дату народження.");
      return;
    }

    fetchWithAuth("/api/user/create_users", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        firstName,
        lastName,
        email,
        password,
        schoolName,
        className,
        role,
        dateOfBirth,
      }),
    })
      .then((res) => {
        if (!res.ok) throw new Error();
        toastr.success(`Користувача у вашій школі успішно створено.`);
        document.getElementById("create-user-form").reset();
      })
      .catch(() => toastr.error("Не вдалося створити користувача."));
  });

document.getElementById("vote-create-form").addEventListener("submit", async (e) => {
  e.preventDefault();

  const form = e.target;
  const title = form["vote-title"].value.trim();
  const description = form["vote-description"].value.trim();
  const level = form["vote-level"].value;
  const startDate = form["vote-startDate"].value;
  const endDate = form["vote-endDate"].value;

  try {
    const response = await fetch("/api/votes/create", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ title, description, votingLevel: level, startDate, endDate }),
    });

    if (response.ok) {
      const createdVote = await response.json();

      if (createdVote.votingLevel === "SCHOOL") {
        await fetch(`/api/invitations/createVoteInvitationForSchool?voteId=${createdVote.id}`, { method: "POST" });
      } else if (createdVote.votingLevel === "CLASS") {
        const className = form["vote-class"].value.trim();
        await fetch(`/api/invitations/createVoteInvitationForClass?voteId=${createdVote.id}&className=${encodeURIComponent(className)}`, { method: "POST" });
      } else if (createdVote.votingLevel === "TEACHERS_GROUP") {
        await fetch(`/api/invitations/createVoteInvitationForTeachers?voteId=${createdVote.id}`, { method: "POST" });
      } else if (createdVote.votingLevel === "SELECTED") {
        const checked = document.querySelectorAll(".people-checkbox:checked");
        const selectedIds = Array.from(checked).map(cb => parseInt(cb.value));

        if (selectedIds.length === 0) {
          toastr.error("Виберіть хоча б одного користувача.");
          return;
        }

        await fetch(`/api/invitations/createVoteInvitation?voteId=${createdVote.id}`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(selectedIds),
        });
        toastr.success("Інвайти надіслані вибраним користувачам!");
      }

      toastr.success("Голосування створено успішно!");
      form.reset();
      document.getElementById("vote-options-list").innerHTML = `
        <input class="vote-option" name="option" placeholder="Варіант 1" required>
        <input class="vote-option" name="option" placeholder="Варіант 2" required>
      `;
    } else {
      const errorText = await response.text();
      toastr.error("Помилка: " + errorText);
    }
  } catch (error) {
    console.error("Помилка при створенні голосування:", error);
    toastr.error("Не вдалося створити голосування.");
  }
});

async function loadTeachersForInvitations() {
  try {
    const response = await fetch("/api/people?role=TEACHER");
    const teachers = await response.json();
    const teacherList = document.getElementById("teacher-list");
    teacherList.innerHTML = teachers.map(t =>
      `<label><input type="checkbox" class="teacher-checkbox" value="${t.id}"> ${t.firstName} ${t.lastName} (${t.email})</label><br>`
    ).join("");
  } catch (error) {
    console.error("Помилка при завантаженні вчителів:", error);
  }
}

async function loadPeople(role = "ALL") {
  try {
    const response = await fetch(`/api/people?role=${role}`);
    const people = await response.json();
    const peopleList = document.getElementById("people-list");
    peopleList.innerHTML = people.map(p =>
      `<label><input type="checkbox" class="people-checkbox" value="${p.id}"> ${p.firstName} ${p.lastName} (${p.email}) [${p.role}]</label><br>`
    ).join("");
  } catch (error) {
    console.error("Помилка при завантаженні користувачів:", error);
  }
}

document.getElementById("vote-level").addEventListener("change", (e) => {
  const extraContainer = document.getElementById("vote-level-extra");
  extraContainer.innerHTML = "";

  if (e.target.value === "CLASS") {
    extraContainer.innerHTML = `<input name="vote-class" placeholder="Назва класу" required>`;
  } else if (e.target.value === "TEACHERS_GROUP") {
    extraContainer.innerHTML = `<div id="teacher-list"></div>`;
    loadTeachersForInvitations();
  } else if (e.target.value === "SELECTED") {
    extraContainer.innerHTML = `
      <label>Фільтр по ролі:</label>
      <select id="role-filter">
        <option value="ALL">Всі</option>
        <option value="STUDENT">Учні</option>
        <option value="PARENT">Батьки</option>
        <option value="TEACHER">Вчителі</option>
      </select>
      <div id="people-list"></div>
    `;
    loadPeople();

    document.getElementById("role-filter").addEventListener("change", (ev) => {
      loadPeople(ev.target.value);
    });
  }
});

document.getElementById("add-vote-option").addEventListener("click", () => {
  const list = document.getElementById("vote-options-list");
  const input = document.createElement("input");
  input.className = "vote-option";
  input.name = "option";
  input.placeholder = `Варіант ${list.children.length + 1}`;
  input.required = true;
  list.appendChild(input);
});

document
  .getElementById("create-event-form")
  .addEventListener("submit", async function (e) {
    e.preventDefault();

    const form = e.target;

    const className = form["event-class-name"].value.trim();
    const title = form["event-title"].value.trim();
    const content = form["event-content"].value.trim();
    const locationOrLink = form["event-locationORlink"].value.trim();
    const startDate = form["event-startDate"].value;
    const duration = parseInt(form["event-duration"].value, 10);
    const eventType = document.getElementById("event-type").value;

    if (!eventType) {
      toastr.error("Будь ласка, виберіть тип події.");
      return;
    }

    let classId = null;
    try {
      const classResp = await fetch(
        `/api/school/getClassIdByName?name=${encodeURIComponent(className)}`
      );
      if (classResp.ok) {
        classId = await classResp.json();
      } else {
        toastr.error("Клас не знайдено");
        return;
      }
    } catch (err) {
      console.error("Помилка при отриманні класу:", err);
      toastr.error("Не вдалося отримати ID класу");
      return;
    }

    const eventData = {
      title,
      content,
      locationOrLink,
      startEvent: startDate,
      duration,
      eventType,
      classId,
    };

    try {
      const response = await fetch("/api/event/events", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(eventData),
      });

      if (!response.ok) {
        const text = await response.text();
        throw new Error(text || "Помилка створення події");
      }

      const createdEvent = await response.json();

      if (!createdEvent?.id) {
        throw new Error("Невірна відповідь сервера при створенні події.");
      }

      const fileInput = document.getElementById("event-file");
      const file = fileInput.files[0];

      if (file) {
        const formData = new FormData();
        formData.append("file", file);

        const uploadResp = await fetch(`/api/event/${createdEvent.id}/upload`, {
          method: "POST",
          body: formData,
        });

        if (!uploadResp.ok) {
          toastr.warning("Подію створено, але файл не завантажено");
        }
      }

      toastr.success("Подію створено успішно!");
      form.reset();
      document.getElementById("event-file").value = "";
    } catch (err) {
      console.error("Помилка при створенні події:", err);
      toastr.error("Не вдалося створити подію: " + err.message);
    }
  });

function renderList(
  listElement,
  items,
  emptyMessage = "Немає даних для відображення."
) {
  listElement.innerHTML = "";

  if (items && items.length > 0) {
    items.forEach((item) => {
      const li = document.createElement("li");
      li.textContent = item;
      listElement.appendChild(li);
    });
  } else {
    const li = document.createElement("li");
    li.textContent = emptyMessage;
    li.style.fontStyle = "italic";
    li.style.color = "#777";
    listElement.appendChild(li);
  }
}

function loadProfile() {
  const profile = document.getElementById("profile-section");

  try {
    fetchWithAuth("/api/user/myProfile")
      .then((response) => {
        if (!response.ok) {
          throw new Error("Не вдалося отримати профіль");
        }
        return response.json();
      })
      .then((user) => {
        document.getElementById("profile-firstName").textContent =
          user.firstName || "-";
        document.getElementById("profile-lastName").textContent =
          user.lastName || "-";
        const rawDate = user.dateOfBirth;
        if (rawDate) {
          const date = new Date(rawDate);
          const formatted = date.toLocaleDateString("uk-UA");
          document.getElementById("profile-dateOfBirth").textContent =
            formatted;
        } else {
          document.getElementById("profile-dateOfBirth").textContent = "-";
        }
        document.getElementById("profile-aboutMe").textContent =
          user.aboutMe || "-";
        document.getElementById("profile-schoolName").textContent =
          user.schoolName || "-";
        document.getElementById("profile-email").textContent =
          user.email || "-";
        document.getElementById("profile-role").textContent = user.role || "-";
      })
      .catch((error) => {
        console.error("Помилка при завантаженні профілю:", error);
      });
  } catch (error) {
    console.error("Несподівана помилка:", error);
    toastr.error("Щось пішло не так.");
  }
}

function loadClasses() {
  const classesList = document.getElementById("classes-list");
  try {
    const url = new URL("/api/school/classes", window.location.origin);

    fetchWithAuth(url.toString())
      .then((response) => {
        if (!response.ok) throw new Error("Network response was not ok");
        return response.json();
      })
      .then((data) => {
        if (data.length > 0) {
          const classNames = data.map((cls) => `${cls.name}`);
          renderList(classesList, classNames);
        } else {
          renderList(
            classesList,
            [],
            `Класи для школи "${schoolName}" не знайдено.`
          );
        }
      });
  } catch (error) {
    console.error("Помилка при завантаженні класів:", error);
  }
}

function loadTeachers(className = "") {
  const teachersList = document.getElementById("teachers-list");
  try {
    const url = new URL("/api/user/teachers", window.location.origin);

    if (className) {
      url.searchParams.append("className", className);
    }

    fetchWithAuth(url.toString())
      .then((response) => {
        if (!response.ok) throw new Error("Network response was not ok");
        return response.json();
      })
      .then((data) => {
        if (data.length > 0) {
          const teacherNames = data.map(
            (teacher) =>
              `${teacher.firstName} ${teacher.lastName} ${teacher.email}`
          );
          renderList(teachersList, teacherNames);
        } else {
          renderList(
            teachersList,
            [],
            `Вчителі для класу "${className}" не знайдені.`
          );
        }
      });
  } catch (error) {
    console.error("Помилка при завантаженні вчителів:", error);
  }
}
function loadStudents(className = "") {
  const studentsList = document.getElementById("students-list");
  try {
    const url = new URL(`/api/user/students`, window.location.origin);

    if (className) {
      url.searchParams.append("className", className);
    }

    fetchWithAuth(url.toString())
      .then((response) => {
        if (!response.ok) throw new Error("Network response was not ok");
        return response.json();
      })
      .then((data) => {
        if (data.length > 0) {
          const studentNames = data.map(
            (student) =>
              `${student.firstName} ${student.lastName} ${student.email}`
          );
          renderList(studentsList, studentNames);
        } else {
          renderList(
            studentsList,
            [],
            `Учні для класу "${className}" не знайдені.`
          );
        }
      });
  } catch (error) {
    console.error("Помилка при завантаженні учнів:", error);
  }
}
function loadParents(className = "") {
  const parentsList = document.getElementById("parents-list");
  try {
    const url = new URL(`/api/user/parents`, window.location.origin);

    if (className) {
      url.searchParams.append("className", className);
    }

    fetchWithAuth(url.toString())
      .then((response) => {
        if (!response.ok) throw new Error("Network response was not ok");
        return response.json();
      })
      .then((data) => {
        if (data.length > 0) {
          const parentNames = data.map(
            (parent) => `${parent.firstName} ${parent.lastName} ${parent.email}`
          );
          renderList(parentsList, parentNames);
        } else {
          renderList(
            parentsList,
            [],
            `Батьки для класу "${className}" не знайдені.`
          );
        }
      });
  } catch (error) {
    console.error("Помилка при завантаженні батьків:", error);
  }
}
function loadVotes() {
  const votesList = document.getElementById("votes-list");
  votesList.innerHTML = "";

  fetchWithAuth("/api/vote/votes")
    .then((response) => {
      if (!response.ok) throw new Error("Не вдалося отримати голосування");
      return response.json();
    })
    .then((data) => {
      if (data.length > 0) {
        data.forEach((vote) => {
          const li = document.createElement("li");
          li.classList.add("vote-item");
          li.innerHTML = `
            <h4>${vote.title}</h4>
            <p>${vote.description}</p>
            <p>Старт: ${new Date(vote.startDate).toLocaleDateString()}</p>
            <p>Кінець: ${new Date(vote.endDate).toLocaleDateString()}</p>
            <p>Варіанти: ${vote.variants.map((v) => v.text).join(", ")}</p>
          `;
          votesList.appendChild(li);
        });
      } else {
        votesList.innerHTML = "<li>Голосування не знайдено.</li>";
      }
    })
    .catch((error) => {
      console.error("Помилка при завантаженні голосувань:", error);
    });
}

function updatePetitionDecision(petitionId, action) {
  let endpoint;
  if (action === "APPROVED") {
    endpoint = `/api/petitions/${petitionId}/directorApprove`;
  } else {
    endpoint = `/api/petitions/${petitionId}/directorReject`;
  }

  fetchWithAuth(endpoint, { method: "PATCH" })
    .then((response) => {
      if (!response.ok) throw new Error("Не вдалося оновити рішення");
      toastr.success(
        action === "APPROVED" ? "Петицію схвалено" : "Петицію відхилено"
      );
      loadPetition();
    })
    .catch((error) => {
      console.error(error);
      toastr.error("Помилка при оновленні рішення.");
    });
}

function loadPetition(className = "") {
  const petitionList = document.getElementById("petition-list");
  petitionList.innerHTML = "";

  const url = new URL("/api/petitions/petitionsForDirector", window.location.origin);
  if (className) url.searchParams.append("className", className);

  fetchWithAuth(url.toString())
    .then((response) => {
      if (!response.ok) throw new Error("Не вдалося отримати заяви");
      return response.json();
    })
    .then((data) => {
      if (data.length > 0) {
        data.forEach((petition) => {
          const li = document.createElement("li");
          li.classList.add("petition-item");

          const header = document.createElement("div");
          header.classList.add("petition-header");
          header.innerHTML = `
            <span class="petition-title">${petition.title}</span>
            <button class="toggle-details">▼</button>
            <button class="toggle-petition-comments">▼</button>
          `;

          const details = document.createElement("div");
          details.classList.add("petition-details");
          details.style.display = "none";
          details.innerHTML = `
            <p><strong>Опис:</strong> ${petition.description}</p>
            <p><strong>Голосів "За":</strong> ${petition.currentPositiveVoteCount}</p>
            <p><strong>Дата створення:</strong> ${new Date(petition.startDate).toLocaleDateString()}</p>
            <button class="accept-btn">✅ Прийняти</button>
            <button class="reject-btn">❌ Відхилити</button>
          `;

          const comments = document.createElement("div");
          comments.id = "petitionComments-list-" + petition.id;
          comments.classList.add("petition-comments");
          comments.style.display = "none";
          comments.innerHTML = `
            <ul class="petitionComments-list"></ul>
            <input type="text" class="petitionComment-input" placeholder="Ваш коментар..." />
            <button class="write-petitionComment">Написати</button>
          `;

          fetchWithAuth(`/api/user/users/${petition.createdBy}`)
            .then((r) => r.json())
            .then((user) => {
              details.innerHTML += `<p><strong>Автор:</strong> ${user.email}</p>`;
            });

          header.querySelector(".toggle-details")
            .addEventListener("click", () => {
              details.style.display = details.style.display === "none" ? "block" : "none";
            });

          details.querySelector(".accept-btn")
            .addEventListener("click", () => updatePetitionDecision(petition.id, "APPROVED"));
          details.querySelector(".reject-btn")
            .addEventListener("click", () => updatePetitionDecision(petition.id, "REJECTED"));

          header.querySelector(".toggle-petition-comments")
            .addEventListener("click", () => {
              comments.style.display = comments.style.display === "none" ? "block" : "none";
              if (comments.style.display === "block") loadPetitionComments(petition, comments);
            });

          li.appendChild(header);
          li.appendChild(details);
          li.appendChild(comments);
          petitionList.appendChild(li);
        });
      } else {
        petitionList.innerHTML = "<li>Заявки для школи не знайдені.</li>";
      }
    })
    .catch((error) => {
      console.error("Помилка при завантаженні заявок:", error);
    });
}

function loadPetitionComments(petition, commentsContainer) {
  const petitionCommentsList = commentsContainer.querySelector(".petitionComments-list");
  petitionCommentsList.innerHTML = '';

  fetchWithAuth(`/api/petitions/comments/petition/${petition.id}`)
    .then(response => {
      if (!response.ok) throw new Error("Не вдалося отримати коментарі");
      return response.json();
    })
    .then((data) => {
      if (data.length > 0) {
        data.forEach((comment) => {
          const li = document.createElement("li");
          li.classList.add("comment-item");
          li.innerHTML = `
            <p>${comment.text}</p>
            <p>Написано: ${new Date(comment.createdAt).toLocaleDateString()}</p>
            <p>Написав: ${comment.email}</p>
          `;
          petitionCommentsList.appendChild(li);
        });
      } else {
        petitionCommentsList.innerHTML = "<li>Коментарів не знайдено.</li>";
      }
    })
    .catch((error) => {
      console.error("Помилка при завантаженні коментарів:", error);
    });
}

function loadEvents() {
  const eventList = document.getElementById("events-list");
  eventList.innerHTML = "";

  fetchWithAuth('/api/event/getEvents')
    .then((response) => {
      if (!response.ok) throw new Error("Не вдалося отримати події");
      return response.json();
    })
    .then((data) => {
      if (data.length > 0) {
        data.forEach((event) => {
          const li = document.createElement("li");
          li.classList.add("event-item");

          const header = document.createElement("div");
          header.classList.add("event-header");
          header.innerHTML = `
            <span class="event-title">${event.title}</span>
            <button class="toggle-details">Деталі</button>
            <button class="toggle-comments">Коментарі</button>
          `;

          const details = document.createElement("div");
          details.classList.add("event-details");
          details.style.display = "none";

          fetchWithAuth(`/api/user/users/${event.createdBy}`)
            .then((r) => r.json())
            .then((user) => {
              details.innerHTML += `<p><strong>Автор:</strong> ${user.email}</p>`;
            });

          fetchWithAuth(`/api/event/${event.id}/files`)
            .then(res => res.json())
            .then(file => {
              details.innerHTML += `<p><strong>Файл:</strong> ${file.fileName}.${file.fileType}</p>`;
              details.innerHTML += `<button class="download-btn">Завантажити файл</button>`;
            });

          const comments = document.createElement("div");
          comments.id = "eventComments-list-" + event.id;
          comments.classList.add("event-comments");
          comments.style.display = "none";
          comments.innerHTML = `
            <ul class="eventComments-list"></ul>
            <input type="text" class="comment-input" placeholder="Ваш коментар..." />
            <button class="write-comment">Написати</button>
          `;

          header.querySelector(".toggle-details")
            .addEventListener("click", () => {
              details.style.display =
                details.style.display === "none" ? "block" : "none";
            });

          header.querySelector(".toggle-comments")
            .addEventListener("click", () => {
              comments.style.display =
                comments.style.display === "none" ? "block" : "none";
              if (comments.style.display === "block") {
                loadEventComments(event, comments);
              }
            });

          details.addEventListener("click", (e) => {
            if (e.target.classList.contains("download-btn")) {
              fetchWithAuth(`/api/event/downloadFiles/${event.id}`)
                .then(resp => {
                  if (!resp.ok) throw new Error("Не вдалося завантажити файли");
                  return resp.blob();
                })
                .then(blob => {
                  const url = window.URL.createObjectURL(blob);
                  const a = document.createElement("a");
                  a.href = url;
                  a.download = `event-${event.id}-files.zip`;
                  document.body.appendChild(a);
                  a.click();
                  a.remove();
                  window.URL.revokeObjectURL(url);
                })
                .catch(err => console.error("Помилка при завантаженні:", err));
            }
          });

          comments.querySelector(".write-comment")
            .addEventListener("click", () => {
              const text = comments.querySelector(".comment-input").value;
              if (!text) return;

              fetchWithAuth(`/api/event/writeComments/${event.id}`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ text })
              })
                .then(res => {
                  if (!res.ok) throw new Error("Не вдалося зберегти коментар");
                  return res.json();
                })
                .then(saved => {
                  const li = document.createElement("li");
                  li.classList.add("comment-item");
                  li.innerHTML = `
                    <p>${saved.text}</p>
                    <p><small>Написано: ${saved.email} | ${new Date(saved.createdAt).toLocaleString()}</small></p>
                  `;
                  comments.querySelector(".eventComments-list").appendChild(li);
                  comments.querySelector(".comment-input").value = "";
                })
                .catch(err => console.error("Помилка при збереженні коментаря:", err));
            });

          li.appendChild(header);
          li.appendChild(details);
          li.appendChild(comments);
          eventList.appendChild(li);
        });
      } else {
        eventList.innerHTML = "<li>Подій не знайдено</li>";
      }
    })
    .catch((error) => {
      console.error("Не вдалося завантажити події", error);
    });
}

function loadEventComments(event, commentsContainer) {
  const eventCommentsList = commentsContainer.querySelector(".eventComments-list");
  eventCommentsList.innerHTML = '';

  fetchWithAuth(`/api/event/comments/event/${event.id}`)
    .then(response => {
      if (!response.ok) throw new Error("Не вдалося отримати коментарі");
      return response.json();
    })
    .then((data) => {
      if (data.length > 0) {
        data.forEach((comment) => {
          const li = document.createElement("li");
          li.classList.add("comment-item");
          li.innerHTML = `
            <p>${comment.text}</p>
            <p>Написано о: ${new Date(comment.createdAt).toLocaleDateString()}</p>
            <p>Написав: ${comment.email}</p>
          `;
          eventCommentsList.appendChild(li);
        });
      } else {
        eventCommentsList.innerHTML = "<li>Коментарів не знайдено.</li>";
      }
    })
    .catch((error) => {
      console.error("Помилка при завантаженні коментарів:", error);
    });
}
function loadEventInvitation() {
  const eventInvitationList = document.getElementById("eventInvitation-list");
  eventInvitationList.innerHTML = "";
  fetchWithAuth(`/api/invitations/myInvitations/{event}`)
    .then(response => {
      if (!response.ok) throw new Error("Не вдалося отримати запрошення на події");
      return response.json();
    })
    .then((data) => {
      if (data.length > 0) {
        data.forEach((invitation) => {
          const li = document.createElement("li");
          li.classList.add("invitation-item");
          li.innerHTML = `
            <p>${invitation.eventOrVoteTitle}</p>
            <p>Змінено: ${new Date(invitation.updatedAt).toLocaleDateString()}</p>
            <p>Надіслав: ${invitation.invitedBy}</p>
          `;

          const header = document.createElement("div");
          header.classList.add("eventInvitation-header");
          header.innerHTML = `
            <button class="toggle-details">▼</button>
          `;

          const details = document.createElement("div");
          details.classList.add("eventInvitation-details");
          details.style.display = "none";
          details.innerHTML = `
            <button class="accept-btn">✅ Прийняти</button>
            <button class="reject-btn">❌ Відхилити</button>
          `;

          header.querySelector(".toggle-details")
            .addEventListener("click", () => {
              details.style.display = details.style.display === "none" ? "block" : "none";
            });

          details.querySelector(".accept-btn")
            .addEventListener("click", () => changeEventInvitationStatus(invitation.id, "ACCEPTED"));
          details.querySelector(".reject-btn")
            .addEventListener("click", () => changeEventInvitationStatus(invitation.id, "DECLINED"));

          li.appendChild(header);
          li.appendChild(details);
          eventInvitationList.appendChild(li);
        });
      } else {
        eventInvitationList.innerHTML = "<li>Запрошень на події не знайдено.</li>";
      }
    })
    .catch((error) => {
      console.error("Помилка при завантаженні запрошення на події:", error);
    });
}
function changeEventInvitationStatus(invitationId, action) {
  let endpoint = `/api/invitations/changeStatus/${invitationId}`;

  fetchWithAuth(endpoint, { method: "PATCH" })
    .then((response) => {
      if (!response.ok) throw new Error("Не вдалося оновити рішення");
      toastr.success(
        action === "ACCEPTED" ? "Ви погодились відвідати подію" : "Ви не погодились відвідати подію"
      );
      loadEventInvitation();
    })
    .catch((error) => {
      console.error(error);
      toastr.error("Помилка при оновленні рішення.");
    });
}
function loadVoteInvitation() {
  const voteInvitationList = document.getElementById("voteInvitation-list");
  voteInvitationList.innerHTML = "";
  fetchWithAuth(`/api/invitations/myInvitations/{vote}`)
    .then(response => {
      if (!response.ok) throw new Error("Не вдалося отримати запрошення на голосування");
      return response.json();
    })
    .then((data) => {
      if (data.length > 0) {
        data.forEach((invitation) => {
          const li = document.createElement("li");
          li.classList.add("invitationV-item");
          li.innerHTML = `
            <p>${invitation.eventOrVoteTitle}</p>
            <p>Змінено: ${new Date(invitation.updatedAt).toLocaleDateString()}</p>
            <p>Надіслав: ${invitation.invitedBy}</p>
          `;

          voteInvitationList.appendChild(li);
        });
      } else {
        voteInvitationList.innerHTML = "<li>Запрошень на голосування не знайдено.</li>";
      }
    })
    .catch((error) => {
      console.error("Помилка при завантаженні запрошення на голосування:", error);
    });
}
document.addEventListener("DOMContentLoaded", () => {
  loadProfile();
  loadClasses();
  loadTeachers();
  loadPetition();
  loadVotes();
  loadEvents();
  loadEventInvitation();
  loadVoteInvitation();

  connectStompWebSocket();

  window.addEventListener("beforeunload", disconnectStompWebSocket);

  const teachersClassInput = document.getElementById("teachers-class-input");
  if (teachersClassInput) {
    const updateTeachersList = () => {
      const className = teachersClassInput.value.trim();
      loadTeachers(className);
    };
    teachersClassInput.addEventListener("input", updateTeachersList);
  }

  const studentClassInput = document.getElementById("students-class-input");
  if (studentClassInput) {
    const updateStudentsList = () => {
      const className = studentClassInput.value.trim();
      loadStudents(className);
    };
    studentClassInput.addEventListener("input", updateStudentsList);
  }

  const parentClassInput = document.getElementById("parents-class-input");
  if (parentClassInput) {
    const updateParentsList = () => {
      const className = parentClassInput.value.trim();
      loadParents(className);
    };
    parentClassInput.addEventListener("input", updateParentsList);
  }

  const petitionClassInput = document.getElementById("petition-class-input");
  if (petitionClassInput) {
    const updatePetitionList = () => {
      const className = petitionClassInput.value.trim();
      loadPetition(className);
    };
    petitionClassInput.addEventListener("input", updatePetitionList);
  }
});

toastr.options = {
  closeButton: true,
  debug: false,
  newestOnTop: false,
  progressBar: true,
  positionClass: "toast-top-center",
  preventDuplicates: false,
  onclick: null,
  showDuration: "300",
  hideDuration: "1000",
  timeOut: "5000",
  extendedTimeOut: "1000",
  showEasing: "swing",
  hideEasing: "linear",
  showMethod: "fadeIn",
  hideMethod: "fadeOut",
};
const WEBSOCKET_ENDPOINT = "/ws-stomp";

let stompClient = null;

function connectStompWebSocket() {
  const socket = new SockJS(WEBSOCKET_ENDPOINT);
  stompClient = Stomp.over(socket);

  stompClient.connect(
    {},
    function (frame) {
      console.log("Connected: " + frame);
    },
    function (error) {
      console.error("WebSocket connection error:", error);
      toastr.error(
        "Помилка з'єднання з сервером реального часу. Спробуйте пізніше."
      );
    }
  );
}

function disconnectStompWebSocket() {
  if (stompClient !== null) {
    stompClient.disconnect();
    console.log("Disconnected from WebSocket");
  }
}