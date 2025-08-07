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
    }else if (btn.id === "tab-votes") {
      document.getElementById("votes-section").classList.add("active");
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
logoutButton.addEventListener("click", () => {
  localStorage.clear();
  toastr.success("Ви вийшли з акаунту.");
  window.location.href = "/login.html";
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
    document.getElementById("edit-dateOfBirth").value = user.dateOfBirth || '';
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
  } catch (error) {
    toastr.error("Помилка при оновленні профілю. Спробуйте ще раз.");
  }

});

function renderList(listElement, items, emptyMessage = "Немає даних для відображення.") {
  listElement.innerHTML = '';

  if (items && items.length > 0) {
    items.forEach(item => {
      const li = document.createElement('li');
      li.textContent = item;
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
        document.getElementById('profile-schoolName').textContent = user.schoolName || '-';
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

function loadClasses() {
  const classesList = document.getElementById('classes-list');
  try {
    const url = new URL('/api/school/classes', window.location.origin);

    fetchWithAuth(url.toString())
      .then(response => {
        if (!response.ok) throw new Error('Network response was not ok');
        return response.json();
      })
      .then(data => {
        if (data.length > 0) {
          const classNames = data.map(cls => `${cls.name}`);
          renderList(classesList, classNames);
        } else {
          renderList(classesList, [], `Класи для школи "${schoolName}" не знайдено.`);
        }
      });
  } catch (error) {
    console.error("Помилка при завантаженні класів:", error);
    toastr.error("Не вдалося завантажити список класів.");
  }
}

function loadTeachers(className = '') {
  const teachersList = document.getElementById('teachers-list');
  try {
    const url = new URL('/api/user/teachers', window.location.origin);

    if (className) {
      url.searchParams.append('className', className);
    }

    fetchWithAuth(url.toString())
      .then(response => {
        if (!response.ok) throw new Error('Network response was not ok');
        return response.json();
      })
      .then(data => {
        if (data.length > 0) {
          const teacherNames = data.map(teacher => `${teacher.firstName} ${teacher.lastName} ${teacher.email}`);
          renderList(teachersList, teacherNames);
        } else {
          renderList(teachersList, [], `Вчителі для класу "${className}" не знайдені.`);
        }
      });
  } catch (error) {
    console.error("Помилка при завантаженні вчителів:", error);
    toastr.error("Не вдалося завантажити список вчителів.");
  }
}
function loadStudents(className = '') {
  const studentsList = document.getElementById('students-list');
  try {
    const url = new URL(`/api/user/students`, window.location.origin);

    if (className) {
      url.searchParams.append('className', className);
    }

    fetchWithAuth(url.toString())
      .then(response => {
        if (!response.ok) throw new Error('Network response was not ok');
        return response.json();
      })
      .then(data => {
        if (data.length > 0) {
          const studentNames = data.map(student => `${student.firstName} ${student.lastName} ${student.email}`);
          renderList(studentsList, studentNames);
        } else {
          renderList(studentsList, [], `Учні для класу "${className}" не знайдені.`);
        }
      });
  } catch (error) {
    console.error("Помилка при завантаженні учнів:", error);
    toastr.error("Не вдалося завантажити список учнів.");
  }
}
function loadParents(className = '') {
  const parentsList = document.getElementById('parents-list');
  try {
    const url = new URL(`/api/user/parents`, window.location.origin);

    if (className) {
      url.searchParams.append('className', className);
    }

    fetchWithAuth(url.toString())
      .then(response => {
        if (!response.ok) throw new Error('Network response was not ok');
        return response.json();
      })
      .then(data => {
        if (data.length > 0) {
          const parentNames = data.map(parent => `${parent.firstName} ${parent.lastName} ${parent.email}`);
          renderList(parentsList, parentNames);
        } else {
          renderList(parentsList, [], `Батьки для класу "${className}" не знайдені.`);
        }
      });
  } catch (error) {
    console.error("Помилка при завантаженні батьків:", error);
    toastr.error("Не вдалося завантажити список батьків.");
  }
}

function loadPetition(className = '') {
  const petitionList = document.getElementById('petition-list');
  try {
    const url = new URL('/api/petitions/petitionsForDirector', window.location.origin);

    if (className) {
      url.searchParams.append('className', className);
    }

    fetchWithAuth(url.toString())
      .then(response => {
        if (!response.ok) throw new Error('Network response was not ok');
        return response.json();
      })
      .then(data => {
        if (data.length > 0) {
          const petitionNames = data.map(petition => `${petition.title}`);
          renderList(petitionList, petitionNames);
        } else {
          renderList(petitionList, [], `Заявки для школи не знайдені.`);
        }
      });
  } catch (error) {
    console.error("Помилка при завантаженні заявок:", error);
    toastr.error("Не вдалося завантажити список заявок.");
  }
}

document.addEventListener('DOMContentLoaded', () => {
  loadProfile();
  loadClasses();
  loadTeachers();
  loadPetition();

  connectStompWebSocket();

  window.addEventListener('beforeunload', disconnectStompWebSocket);

  const teachersClassInput = document.getElementById('teachers-class-input');
  if (teachersClassInput) {
    const updateTeachersList = () => {
      const className = teachersClassInput.value.trim();
      loadTeachers(className);
    };
    teachersClassInput.addEventListener('input', updateTeachersList);
  }

  const studentClassInput = document.getElementById('students-class-input');
  if (studentClassInput) {
    const updateStudentsList = () => {
      const className = studentClassInput.value.trim();
      loadStudents(className);
    };
    studentClassInput.addEventListener('input', updateStudentsList);
  }

  const parentClassInput = document.getElementById('parents-class-input');
  if (parentClassInput) {
    const updateParentsList = () => {
      const className = parentClassInput.value.trim();
      loadParents(className);
    };
    parentClassInput.addEventListener('input', updateParentsList);
  }

  const petitionClassInput = document.getElementById('petition-class-input');
  if (petitionClassInput) {
    const updatePetitionList = () => {
      const className = petitionClassInput.value.trim();
      loadPetition(className);
    };
    petitionClassInput.addEventListener('input', updatePetitionList);
  }
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
const WEBSOCKET_ENDPOINT = '/ws-stomp';

let stompClient = null;

function connectStompWebSocket() {
  const socket = new SockJS(WEBSOCKET_ENDPOINT);
  stompClient = Stomp.over(socket);

  stompClient.connect({}, function (frame) {
    console.log('Connected: ' + frame);

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

    stompClient.subscribe('/topic/classes/created', function (message) {
      const newClass = JSON.parse(message.body);
      console.log('WebSocket: Створено новий клас:', newClass);
      loadClasses(document.getElementById('classes-school-input').value.trim());
    });

    stompClient.subscribe('/topic/users/create/error', function (message) {
      const errorMessage = message.body;
      console.error('WebSocket Error: Помилка створення користувача:', errorMessage);
    });

    stompClient.subscribe('/topic/users/profile/error', function (message) {
      const errorMessage = message.body;
      console.error('WebSocket Error: Помилка профілю:', errorMessage);
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