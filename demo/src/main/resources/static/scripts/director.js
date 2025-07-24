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
    } else if (btn.id === "tab-petitions"){
      document.getElementById("petitions-section").classList.add("active");
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

document.getElementById("create-class-form").addEventListener("submit", (e) => {
  e.preventDefault();

  const className = document.getElementById("class-name").value.trim();
  if (!className) {
    toastr.error("Будь ласка, введіть назву класу.");
    return;
  }

  fetchWithAuth("/api/school/create-class", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ schoolId, className })
  })
    .then((res) => {
      if (!res.ok) throw new Error();
      toastr.success(`Клас ${className} у школі ${schoolName} успішно створено.`);
      document.getElementById("create-class-form").reset();
    })
    .catch((e) => toastr.error("Не вдалося створити клас." + e.message));

});


document.getElementById("create-user-form").addEventListener("submit", (e) => {
  e.preventDefault();

  const firstName = document.getElementById("user-first-name").value.trim();
  const lastName = document.getElementById("user-last-name").value.trim();
  const email = document.getElementById("user-email").value.trim();
  const password = document.getElementById("user-password").value.trim();
  const className = document.getElementById("user-class").value.trim();
  const role = document.getElementById("user-role").value.trim();
  const dateOfBirth = document.getElementById("user-dateOfBirth").value.trim();
  const schoolName = user.schoolName;

  if (!firstName) {
    toastr.error("Будь ласка, введіть ім'я.");
    return;
  } if (!lastName) {
    toastr.error("Будь ласка, введіть прізвище.");
    return;
  } if (!email) {
    toastr.error("Будь ласка, введіть email.");
    return;
  } if (!password) {
    toastr.error("Будь ласка, введіть пароль.");
    return;
  } if (!role) {
    toastr.error("Будь ласка, оберіть роль для користувача.");
    return;
  } if (!dateOfBirth) {
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
      dateOfBirth
    })
  })
    .then((res) => {
      if (!res.ok) throw new Error();
      toastr.success(`Користувача у вашій школі успішно створено.`);
      document.getElementById("create-user-form").reset();
    })
    .catch(() => toastr.error("Не вдалося створити користувача."));
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
        document.getElementById('profile-dateOfBirth').textContent = user.dateOfBirth || '-';
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
          renderList(teachersList, [], `Вчителі для школи "${schoolName}" та класу "${className}" не знайдені.`);
        }
      });
  } catch (error) {
    console.error("Помилка при завантаженні вчителів:", error);
    toastr.error("Не вдалося завантажити список вчителів.");
  }
}
function loadStudents(className=''){
  const studentsList = document.getElementById('students-list');
  try {
    const role = 'STUDENT';
    const url = new URL(`/api/user/users/role/school/${role}`, window.location.origin);

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
          renderList(studentsList, [], `Учні для школи "${schoolName}" та класу "${className}" не знайдені.`);
        }
      });
  } catch (error) {
    console.error("Помилка при завантаженні учнів:", error);
    toastr.error("Не вдалося завантажити список учнів.");
  }
}
function loadParents(className=''){
  const parentsList = document.getElementById('parents-list');
  try {
    const role = 'PARENT';
    const url = new URL(`/api/user/users/role/school/${role}`, window.location.origin);

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
          const parentNames = data.map( parent => `${ parent.firstName} ${ parent.lastName} ${ parent.email}`);
          renderList( parentsList,  parentNames);
        } else {
          renderList( parentsList, [], `Батьків для школи "${schoolName}" та класу "${className}" не знайдені.`);
        }
      });
  } catch (error) {
    console.error("Помилка при завантаженні батьків:", error);
    toastr.error("Не вдалося завантажити список батьків.");
  }
}
document.addEventListener('DOMContentLoaded', () => {
  loadProfile();
  loadClasses();
  loadTeachers();
  loadStudents();
  loadParents();

  const teachersClassInput = document.getElementById('teachers-class-input');
  if ( teachersClassInput) {
    const updateTeachersList = () => {
      const className = teachersClassInput.value.trim();
      loadTeachers(className);
    };
    teachersClassInput.addEventListener('input', updateTeachersList);
  }

  const studentClassInput = document.getElementById('students-class-input');
  if ( studentClassInput) {
    const updateStudentsList = () => {
      const className = studentClassInput.value.trim();
      loadStudents(className);
    };
    teachersClassInput.addEventListener('input', studentClassInput);
  }
  const parentClassInput = document.getElementById('parents-class-input');
  if (parentClassInput) {
    const updateParentsList = () => {
      const className = parentClassInput.value.trim();
      loadTeachers(school, className);
    };
    parentClassInput.addEventListener('input', updateParentsList);
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
