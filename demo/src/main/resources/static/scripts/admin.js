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
logoutButton.addEventListener("click", () => {
  localStorage.clear();
  toastr.success("Ви вийшли з акаунту.");
  window.location.href = "/login.html";
});

document.getElementById("openButton").addEventListener("click", () => {
  document.getElementById("updateProfile").style.display = "flex";
});

document.getElementById("closeButton").addEventListener("click", () => {
  document.getElementById("updateProfile").style.display = "none";
});

document.getElementById("editProfileForm").addEventListener("submit", async (e) => {
  e.preventDefault();

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
  } if (!className) {
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
  } if (!lastName) {
    toastr.error("Будь ласка, введіть прізвище.");
    return;
  } if (!email) {
    toastr.error("Будь ласка, введіть email.");
    return;
  } if (!password) {
    toastr.error("Будь ласка, введіть пароль.");
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
  } if (!lastName) {
    toastr.error("Будь ласка, введіть прізвище.");
    return;
  } if (!email) {
    toastr.error("Будь ласка, введіть email.");
    return;
  } if (!password) {
    toastr.error("Будь ласка, введіть пароль.");
    return;
  } if (!schoolName) {
    toastr.error("Будь ласка, введіть назву школи.");
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

function loadAdmins() {
  const adminsList = document.getElementById('admins-list');

  try {
    fetchWithAuth('/api/user/admins')
      .then(response => {
        if (!response.ok) throw new Error('Network response was not ok');
        return response.json();
      })
      .then(data => {
        if (data && data.length > 0) {
          renderList(adminsList, data.map(admin => `${admin.firstName} ${admin.lastName} (${admin.email})`));
        } else {
          renderList(adminsList, [], "Немає адмінів для відображення.");
        }
      })
      .catch(error => {
        console.error("Помилка при завантаженні адмінів:", error);
        toastr.error("Не вдалося завантажити список адмінів.");
      });
  } catch (error) {
    console.error("Помилка при завантаженні адмінів:", error);
    toastr.error("Не вдалося завантажити список адмінів.");
    return;
  }
}
function loadSchools() {
  const schoolsList = document.getElementById('schools-list');

  try {
    fetchWithAuth('/api/school/schools')
      .then(response => {
        if (!response.ok) throw new Error('Network response was not ok');
        return response.json();
      })
      .then(data => {
        if (data && data.length > 0) {
          renderList(schoolsList, data.map(school => school.name));
        } else {
          renderList(schoolsList, [], "Немає шкіл для відображення.");
        }
      })
      .catch(error => {
        console.error("Помилка при завантаженні шкіл:", error);
        toastr.error("Не вдалося завантажити список шкіл.");
      });
  } catch (error) {
    console.error("Помилка при завантаженні шкіл:", error);
    toastr.error("Не вдалося завантажити список шкіл.");
    return;
  }
}
function loadClasses(schoolName = '') {
  const classesList = document.getElementById('classes-list');
  try {
    const url = new URL('/api/school/classes', window.location.origin);
    if (schoolName) {
      url.searchParams.append('schoolName', schoolName);
    }

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

document.addEventListener('DOMContentLoaded', () => {
  loadAdmins();
  loadSchools();
  loadClasses();
  //loadDirectors();
  //loadTeachers();

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

