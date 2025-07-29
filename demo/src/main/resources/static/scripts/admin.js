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
    const response = await fetchWithAuth('/api/user/myProfile');
    if (!response.ok) {
      throw new Error("Не вдалося отримати профіль");
    }
    const user = await response.json();

    document.getElementById("edit-firstName").value = user.firstName || '';
    document.getElementById("edit-lastName").value = user.lastName || '';
    document.getElementById("edit-aboutMe").value = user.aboutMe || '';
    const rawDate = user.dateOfBirth;
    if (rawDate) {
      const date = new Date(rawDate);
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      document.getElementById('edit-dateOfBirth').value = `${year}-${month}-${day}`;
    } else {
      document.getElementById('edit-dateOfBirth').value = '';
    }
    document.getElementById("edit-email").value = user.email || '';
  } catch (error) {
    console.error("Помилка при завантаженні профілю для редагування:", error);
    toastr.error("Не вдалося завантажити дані профілю.");
  }
}

document.getElementById("editProfileForm").addEventListener("submit", async (e) => {
  e.preventDefault();

  const password = document.getElementById("edit-password").value.trim();
  const confirmPassword = document.getElementById("confirm-password").value.trim();
  const firstName = document.getElementById("edit-firstName").value.trim();
  const lastName = document.getElementById("edit-lastName").value.trim();
  const aboutMe = document.getElementById("edit-aboutMe").value.trim();
  const dateOfBirth = document.getElementById("edit-dateOfBirth").value.trim();
  const email = document.getElementById("edit-email").value.trim();
  if (password && password !== confirmPassword) {
    toastr.error("Паролі не співпадають.");
    return;
  }

  try {
    const res = await fetchWithAuth(`/api/user/me`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ firstName, lastName, aboutMe, dateOfBirth, email, password })
    });

    if (!res.ok) throw new Error(res.status);
    toastr.success("Профіль успішно оновлено.");
    document.getElementById("updateProfile").style.display = "none";
    document.getElementById("editProfileForm").reset();
    loadProfile();
  } catch (error) {
    toastr.error("Помилка при оновленні профілю. Спробуйте ще раз.");
  }
});

document.getElementById("create-school-form").addEventListener("submit", (e) => {
  e.preventDefault();

  const schoolName = document.getElementById("school-name").value.trim();

  if (!schoolName) {
    toastr.error("Будь ласка, введіть назву школи.");
    return;
  }

  fetchWithAuth("/api/school/create", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ name: schoolName })
  })
    .then((res) => {
      if (!res.ok) throw new Error();
      toastr.success(`Школу ${schoolName} успішно створено.`);
      document.getElementById("create-school-form").reset();
    })
    .catch(() => toastr.error("Не вдалося створити школу."));
});

document.getElementById("create-class-form").addEventListener("submit", (e) => {
  e.preventDefault();

  const schoolName = document.getElementById("class-school-name").value.trim();
  const className = document.getElementById("class-name").value.trim();
  if (!schoolName) {
    toastr.error("Будь ласка, введіть назву школи.");
    return;
  }
  if (!className) {
    toastr.error("Будь ласка, введіть назву класу.");
    return;
  }

  fetchWithAuth("/api/school/create-class", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ schoolName, className })
  })
    .then((res) => {
      if (!res.ok) throw new Error();
      toastr.success(`Клас ${className} у школі ${schoolName} успішно створено.`);
      document.getElementById("create-class-form").reset();
    })
    .catch((e) => toastr.error("Не вдалося створити клас." + e.message));
});

document.getElementById("create-admin-form").addEventListener("submit", (e) => {
  e.preventDefault();

  const firstName = document.getElementById("admin-first-name").value.trim();
  const lastName = document.getElementById("admin-last-name").value.trim();
  const email = document.getElementById("admin-email").value.trim();
  const password = document.getElementById("admin-password").value.trim();
  const dateOfBirth = document.getElementById("admin-dateOfBirth").value.trim();

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
      role: "ADMIN",
      dateOfBirth
    })
  })
    .then((res) => {
      if (!res.ok) throw new Error();
      toastr.success("Адміна успішно створено.");
      document.getElementById("create-admin-form").reset();
    })
    .catch(() => toastr.error("Не вдалося створити адміна."));
});


document.getElementById("create-user-form").addEventListener("submit", (e) => {
  e.preventDefault();

  const firstName = document.getElementById("user-first-name").value.trim();
  const lastName = document.getElementById("user-last-name").value.trim();
  const email = document.getElementById("user-email").value.trim();
  const password = document.getElementById("user-password").value.trim();
  const schoolName = document.getElementById("user-school").value.trim();
  const role = document.getElementById("user-role").value.trim();
  const dateOfBirth = document.getElementById("user-dateOfBirth").value.trim();

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
  if (!schoolName) {
    toastr.error("Будь ласка, введіть назву школи.");
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
      role,
      dateOfBirth
    })
  })
    .then((res) => {
      if (!res.ok) throw new Error();
      toastr.success(`Користувача у школі ${schoolName} успішно створено.`);
      document.getElementById("create-user-form").reset();
    })
    .catch(() => toastr.error("Не вдалося створити користувача."));
});

toastr.options = {
  "closeButton": true,
  "debug": false,
  "newestOnTop": false,
  "progressBar": true,
  "positionClass": "toast-top-center",
  "preventDuplicates": false,
  "onclick": null,
  "showDuration": "300",
  "hideDuration": "1000",
  "timeOut": "5000",
  "extendedTimeOut": "1000",
  "showEasing": "swing",
  "hideEasing": "linear",
  "showMethod": "fadeIn",
  "hideMethod": "fadeOut"
};

function renderList(listElement, items, renderItemFn, emptyMessage = "Немає даних для відображення.") {
  listElement.innerHTML = '';

  if (items && items.length > 0) {
    items.forEach(item => {
      const li = document.createElement('li');
      li.textContent = renderItemFn(item);
      listElement.appendChild(li);
    });
  } else {
    const li = document.createElement('li');
    li.textContent = emptyMessage;
    li.style.fontStyle = 'italic';
    li.style.color = '#777';
    listElement.appendChild(li);
  }
}

function loadProfile() {
  const profile = document.getElementById('profile-section');

  try {
    fetchWithAuth('/api/user/myProfile')
      .then(response => {
        if (!response.ok) {
          throw new Error('Не вдалося отримати профіль');
        }
        return response.json();
      })
      .then(user => {
        document.getElementById('profile-firstName').textContent = user.firstName || '-';
        document.getElementById('profile-lastName').textContent = user.lastName || '-';
        const rawDate = user.dateOfBirth;
        if (rawDate) {
          const date = new Date(rawDate);
          const formatted = date.toLocaleDateString('uk-UA');
          document.getElementById('profile-dateOfBirth').textContent = formatted;
        } else {
          document.getElementById('profile-dateOfBirth').textContent = '-';
        }
        document.getElementById('profile-aboutMe').textContent = user.aboutMe || '-';
        document.getElementById('profile-email').textContent = user.email || '-';
        document.getElementById('profile-role').textContent = user.role || '-';
      })
      .catch(error => {
        console.error("Помилка при завантаженні профілю:", error);
        toastr.error("Не вдалося завантажити профіль.");
      });
  } catch (error) {
    console.error("Несподівана помилка:", error);
    toastr.error("Щось пішло не так.");
  }
}

function loadAdmins() {
  const adminsList = document.getElementById('admins-list');
  fetchWithAuth('/api/user/admins')
    .then(response => response.json())
    .then(data => {
      renderList(adminsList, data, admin => `${admin.firstName} ${admin.lastName} (${admin.email})`, "Немає адмінів для відображення.");
    })
    .catch(error => {
      console.error("Помилка при завантаженні адмінів:", error);
      toastr.error("Не вдалося завантажити список адмінів.");
    });
}

function loadSchools() {
  const schoolsList = document.getElementById('schools-list');
  fetchWithAuth('/api/school/schools')
    .then(response => response.json())
    .then(data => {
      renderList(schoolsList, data, school => school.name, "Немає шкіл для відображення.");
    })
    .catch(error => {
      console.error("Помилка при завантаженні шкіл:", error);
      toastr.error("Не вдалося завантажити список шкіл.");
    });
}

function loadClasses(schoolName = '') {
  const classesList = document.getElementById('classes-list');
  const url = new URL('/api/school/admin/classes', window.location.origin);
  if (schoolName) {
    url.searchParams.append('schoolName', schoolName);
  }
  fetchWithAuth(url.toString())
    .then(response => response.json())
    .then(data => {
      renderList(classesList, data, cls => cls.name, `Класи для школи "${schoolName}" не знайдено.`);
    })
    .catch(error => {
      console.error("Помилка при завантаженні класів:", error);
      toastr.error("Не вдалося завантажити список класів.");
    });
}

function loadDirectors(schoolName = '') {
  const directorsList = document.getElementById('directors-list');
  const url = new URL('/api/user/admin/directors', window.location.origin);
  if (schoolName) {
    url.searchParams.append('schoolName', schoolName);
  }
  fetchWithAuth(url.toString())
    .then(response => response.json())
    .then(data => {
      renderList(directorsList, data, director => `${director.firstName} ${director.lastName} (${director.email})`, `Директори для школи "${schoolName}" не знайдені.`);
    })
    .catch(error => {
      console.error("Помилка при завантаженні директорів:", error);
      toastr.error("Не вдалося завантажити список директорів.");
    });
}

function loadTeachers(schoolName = '', className = '') {
  const teachersList = document.getElementById('teachers-list');
  const url = new URL('/api/user/admin/teachers', window.location.origin);
  if (schoolName) {
    url.searchParams.append('schoolName', schoolName);
  }
  if (className) {
    url.searchParams.append('className', className);
  }
  fetchWithAuth(url.toString())
    .then(response => response.json())
    .then(data => {
      renderList(teachersList, data, teacher => `${teacher.firstName} ${teacher.lastName} (${teacher.email})`,
        `Вчителі для школи "${schoolName}" та класу "${className}" не знайдені.`);
    })
    .catch(error => {
      console.error("Помилка при завантаженні вчителів:", error);
      toastr.error("Не вдалося завантажити список вчителів.");
    });
}

// --- WebSocket Integration ---
const WEBSOCKET_ENDPOINT = '/ws-stomp';

let stompClient = null;

function connectStompWebSocket() {
  const socket = new SockJS(WEBSOCKET_ENDPOINT);
  stompClient = Stomp.over(socket);

  stompClient.connect({}, function (frame) {
    console.log('Connected: ' + frame);

    stompClient.subscribe('/topic/users/admins/list', function (message) {
      const admins = JSON.parse(message.body);
      console.log('WebSocket: Оновлений список адмінів:', admins);
      const adminsList = document.getElementById('admins-list');
      renderList(adminsList, admins, admin => `${admin.firstName} ${admin.lastName} (${admin.email})`);
    });

    stompClient.subscribe('/topic/users/teachers/list', function (message) {
      const teachers = JSON.parse(message.body);
      console.log('WebSocket: Оновлений список вчителів:', teachers);
      const teachersList = document.getElementById('teachers-list');
      renderList(teachersList, teachers, teacher => `${teacher.firstName} ${teacher.lastName} (${teacher.email})`);
    });

    stompClient.subscribe('/topic/users/students/list', function (message) {
      const students = JSON.parse(message.body);
      console.log('WebSocket: Оновлений список студентів:', students);
      const studentsList = document.getElementById('students-list');
      renderList(studentsList, students, student => `${student.firstName} ${student.lastName} (${student.email})`);
    });

    stompClient.subscribe('/topic/users/parents/list', function (message) {
      const parents = JSON.parse(message.body);
      console.log('WebSocket: Оновлений список батьків:', parents);
      const parentsList = document.getElementById('parents-list');
      renderList(parentsList, parents, parent => `${parent.firstName} ${parent.lastName} (${parent.email})`);
    });

    stompClient.subscribe('/topic/users/created', function (message) {
      const newUser = JSON.parse(message.body);
      console.log('WebSocket: Створено нового користувача:', newUser);
      loadAdmins();
      loadDirectors();
      loadTeachers();
    });

    stompClient.subscribe('/topic/users/updated/id/{id}', function (message) {
      const updatedUser = JSON.parse(message.body);
      console.log('WebSocket: Оновлено користувача:', updatedUser);

    });

    stompClient.subscribe('/topic/users/profile', function (message) {
      const userProfile = JSON.parse(message.body);
      console.log('WebSocket: Оновлення профілю поточного користувача:', userProfile);
      document.getElementById('profile-firstName').textContent = userProfile.firstName || '-';
      document.getElementById('profile-lastName').textContent = userProfile.lastName || '-';
      const rawDate = userProfile.dateOfBirth;
      if (rawDate) {
        const date = new Date(rawDate);
        const formatted = date.toLocaleDateString('uk-UA');
        document.getElementById('profile-dateOfBirth').textContent = formatted;
      } else {
        document.getElementById('profile-dateOfBirth').textContent = '-';
      }
      document.getElementById('profile-aboutMe').textContent = userProfile.aboutMe || '-';
      document.getElementById('profile-email').textContent = userProfile.email || '-';
      document.getElementById('profile-role').textContent = userProfile.role || '-';
    });


    stompClient.subscribe('/topic/schools/list', function (message) {
      const schools = JSON.parse(message.body);
      console.log('WebSocket: Оновлений список шкіл:', schools);
      const schoolsList = document.getElementById('schools-list');
      renderList(schoolsList, schools, school => school.name);
    });

    stompClient.subscribe('/topic/schools/created', function (message) {
      const newSchool = JSON.parse(message.body);
      console.log('WebSocket: Створено нову школу:', newSchool);
      loadSchools();
    });

    stompClient.subscribe('/topic/admin/classes/list', function (message) {
      const classes = JSON.parse(message.body);
      console.log('WebSocket: Оновлений список класів (адмін):', classes);
      const classesList = document.getElementById('classes-list');
      renderList(classesList, classes, cls => cls.name);
    });

    stompClient.subscribe('/topic/classes/created', function (message) {
      const newClass = JSON.parse(message.body);
      console.log('WebSocket: Створено новий клас:', newClass);
      loadClasses(document.getElementById('classes-school-input').value.trim());
    });

    stompClient.subscribe('/topic/users/create/error', function (message) {
      const errorMessage = message.body;
      console.error('WebSocket Error: Помилка створення користувача:', errorMessage);
    });

    stompClient.subscribe('/topic/users/update/error', function (message) {
      const errorMessage = message.body;
      console.error('WebSocket Error: Помилка оновлення користувача:', errorMessage);
    });

    stompClient.subscribe('/topic/users/profile/error', function (message) {
      const errorMessage = message.body;
      console.error('WebSocket Error: Помилка профілю:', errorMessage);
    });

    stompClient.subscribe('/topic/schools/error', function (message) {
      const errorMessage = message.body;
      console.error('WebSocket Error: Помилка шкіл:', errorMessage);
    });

    stompClient.subscribe('/topic/classes/create/error', function (message) {
      const errorMessage = message.body;
      console.error('WebSocket Error: Помилка створення класу:', errorMessage);
    });

  }, function (error) {
    console.error('WebSocket connection error:', error);
    toastr.error("Помилка з'єднання з сервером реального часу. Спробуйте пізніше.");
  });
}

function disconnectStompWebSocket() {
  if (stompClient !== null) {
    stompClient.disconnect();
    console.log("Disconnected from WebSocket");
  }
}

document.addEventListener('DOMContentLoaded', () => {
  loadProfile();
  loadAdmins();
  loadSchools();

  connectStompWebSocket();

  window.addEventListener('beforeunload', disconnectStompWebSocket);

  const classesSchoolInput = document.getElementById('classes-school-input');
  if (classesSchoolInput) {
    classesSchoolInput.addEventListener('input', (event) => {
      loadClasses(event.target.value.trim());
    });
  }

  const directorsSchoolInput = document.getElementById('directors-school-input');
  if (directorsSchoolInput) {
    directorsSchoolInput.addEventListener('input', (event) => {
      loadDirectors(event.target.value.trim());
    });
  }

  const teachersSchoolInput = document.getElementById('teachers-school-input');
  const teachersClassInput = document.getElementById('teachers-class-input');
  if (teachersSchoolInput && teachersClassInput) {
    const updateTeachersList = () => {
      const school = teachersSchoolInput.value.trim();
      const className = teachersClassInput.value.trim();
      loadTeachers(school, className);
    };
    teachersSchoolInput.addEventListener('input', updateTeachersList);
    teachersClassInput.addEventListener('input', updateTeachersList);
  }
});