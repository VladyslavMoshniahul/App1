// === Перемикання вкладок ===
const tabButtons = document.querySelectorAll(".nav-tabs button");
const sections = document.querySelectorAll(".page-section");

tabButtons.forEach((btn) => {
  btn.addEventListener("click", () => {
    // Прибираємо активність з усіх
    tabButtons.forEach((b) => b.classList.remove("active"));
    sections.forEach((s) => s.classList.remove("active"));

    // Додаємо активність для обраної вкладки та секції
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

// === Темна тема ===
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

// Ініціалізація теми при завантаженні
document.addEventListener("DOMContentLoaded", () => {
  const savedTheme = localStorage.getItem("theme") || "light";
  applyTheme(savedTheme);
});

// === Кнопка виходу ===
const logoutButton = document.getElementById("logoutButton");
logoutButton.addEventListener("click", () => {
  // Очистити токен або сесію
  localStorage.clear(); // якщо використовуєш токен
  alert("Ви вийшли з акаунту.");
  window.location.href = "/login.html"; // або на головну
});

// === Валідація оновлення профілю ===
const profileForm = document.getElementById("editProfileForm");
profileForm.addEventListener("submit", (e) => {
  e.preventDefault();

  const password = document.getElementById("edit-password").value.trim();
  const confirmPassword = document.getElementById("confirm-password").value.trim();

  if (password && password !== confirmPassword) {
    alert("Паролі не співпадають.");
    return;
  }

  // Додай тут логіку відправки запиту
  alert("Профіль оновлено (імітація).");
});
