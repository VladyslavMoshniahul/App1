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

document.getElementById("editProfileForm").addEventListener("submit", (e) => {
  e.preventDefault();

  const password = document.getElementById("edit-password").value.trim();
  const confirmPassword = document.getElementById("confirm-password").value.trim();

  if (password && password !== confirmPassword) {
    toastr.success("Паролі не співпадають.");
    return;
  }

  // --- Тут ваша логіка відправки запиту на сервер ---
  toastr.success("Профіль успішно оновлено.");
  document.getElementById("editProfileForm").reset();
  document.getElementById("updateProfile").style.display = "none";
});

document.getElementById("create-school-form").addEventListener("submit", (e) => {
  e.preventDefault();

  const schoolName = document.getElementById("school-name").value.trim();

  if (!schoolName) {
    toastr.error("Будь ласка, введіть назву школи.");
    return;
  }

  // --- Тут ваша логіка відправки запиту на сервер ---

  toastr.success(`Школу ${schoolName} успішно створено.`);
  document.getElementById("create-school-form").reset();

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

  // --- Тут ваша логіка відправки запиту на сервер ---

  toastr.success(`Клас ${className} у школі ${schoolName} успішно створено.`);
  document.getElementById("create-class-form").reset();

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

  // --- Тут ваша логіка відправки запиту на сервер ---

  toastr.success(`Користувача у школі ${schoolName} успішно створено.`);
  document.getElementById("create-user-form").reset();

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

  // --- Тут ваша логіка відправки запиту на сервер ---

  toastr.success(`Адміна успішно створено.`);
  document.getElementById("create-admin-form").reset();

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


