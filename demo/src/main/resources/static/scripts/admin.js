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


