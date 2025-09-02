document.getElementById("loginButton").addEventListener("click", async () => {
  const email = document.getElementById("login").value.trim();
  const pass  = document.getElementById("password").value.trim();
  if (!email || !pass) {
    return toastr.error("Будь ласка, заповніть всі поля.");
  }

  const params = new URLSearchParams();
  params.append("username", email);
  params.append("password", pass);

  try {
    const res = await fetch("/api/login", {
      method: "POST",
      credentials: "include",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({ 
                        email: email,
                        password: pass,
                    })
    });

    if (res.status === 401) {
      return toastr.error("Невірний email або пароль.");
    }
    if (!res.ok) {
      return toastr.error("Помилка: статус " + res.status);
    }

    const { role: realRole } = await res.json();
    switch (realRole) {
      case "TEACHER":
        window.location.href = "teacher.html";
        break;
      case "PARENT":
        window.location.href = "parent.html";
        break;
      case "STUDENT":
        window.location.href = "student.html";
        break;
      case "DIRECTOR":
        window.location.href = "director.html";
        break;
      case "ADMIN":
        window.location.href = "admin.html";
        break;
      default:
        toastr.error("Невідома роль: " + realRole);
    }
  } catch (err) {
    console.error(err);
    toastr.error("Не вдалося увійти.");
  }
});




