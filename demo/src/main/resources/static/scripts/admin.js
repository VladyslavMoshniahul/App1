// scripts/admin.js

document.addEventListener('DOMContentLoaded', () => {
    // --- Theme Toggling ---
    const themeToggle = document.getElementById('themeToggle');
    const body = document.body;

    // Load saved theme preference
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme === 'dark') {
        body.classList.add('dark-theme');
        themeToggle.textContent = '☀️'; // Sun icon for light theme
    } else {
        themeToggle.textContent = '🌙'; // Moon icon for dark theme
    }

    themeToggle.addEventListener('click', () => {
        body.classList.toggle('dark-theme');
        if (body.classList.contains('dark-theme')) {
            localStorage.setItem('theme', 'dark');
            themeToggle.textContent = '☀️';
        } else {
            localStorage.setItem('theme', 'light');
            themeToggle.textContent = '🌙';
        }
    });

    // --- Tab Switching Logic ---
    const tabButtons = document.querySelectorAll('.nav-tabs button');
    const pageSections = document.querySelectorAll('.page-section');

    function activateTab(tabId) {
        tabButtons.forEach(button => {
            button.classList.remove('active');
        });
        pageSections.forEach(section => {
            section.classList.remove('active');
        });

        const targetButton = document.getElementById(tabId);
        const targetSectionId = tabId.replace('tab-', '') + '-section'; // e.g., 'tab-profile' -> 'profile-section'
        const targetSection = document.getElementById(targetSectionId);

        if (targetButton) {
            targetButton.classList.add('active');
        }
        if (targetSection) {
            targetSection.classList.add('active');
        } else if (tabId === 'tab-users') { // Special handling for 'Сторення' tab
            document.getElementById('creating-page').classList.add('active');
        }

        // When switching to a tab that displays data, refresh it
        if (tabId === 'tab-profile') {
            displayProfileInfo();
        } else if (tabId === 'tab-statistic') {
            displayAdminsList();
            displaySchoolsList();
            // The class, director, teacher lists will be updated on input change
        }
    }

    tabButtons.forEach(button => {
        button.addEventListener('click', (event) => {
            activateTab(event.target.id);
        });
    });

    // Initialize with the active tab from HTML (or default to 'tab-users')
    const initialActiveButton = document.querySelector('.nav-tabs button.active');
    if (initialActiveButton) {
        activateTab(initialActiveButton.id);
    } else {
        activateTab('tab-users'); // Default to the creating/users tab if none is active
    }

    // --- Logout Functionality ---
    const logoutButton = document.getElementById('logoutButton');
    logoutButton.addEventListener('click', () => {
        localStorage.removeItem('loggedInUser'); // Clear logged-in user data
        localStorage.removeItem('token'); // Clear any authentication token
        window.location.href = 'login.html'; // Redirect to login or home page
    });

    // --- Mock Data Storage (for demonstration) ---
    // In a real app, this would come from a database via API calls
    let users = JSON.parse(localStorage.getItem('users')) || [];
    let schools = JSON.parse(localStorage.getItem('schools')) || [];
    let classes = JSON.parse(localStorage.getItem('classes')) || [];

    // Simulate a logged-in admin user
    let loggedInUser = JSON.parse(localStorage.getItem('loggedInUser'));
    if (!loggedInUser) {
        // Create a default admin if none exists for demonstration
        const defaultAdmin = {
            id: 'admin123',
            firstName: 'Адмін',
            lastName: 'Тест',
            email: 'admin@example.com',
            password: 'password123', // In real app, never store plain passwords!
            dateOfBirth: '1990-01-01',
            aboutMe: 'Це тестовий обліковий запис адміністратора.',
            role: 'ADMIN'
        };
        // Only add if not already present
        if (!users.some(u => u.email === defaultAdmin.email)) {
            users.push(defaultAdmin);
            localStorage.setItem('users', JSON.stringify(users));
        }
        localStorage.setItem('loggedInUser', JSON.stringify(defaultAdmin));
        loggedInUser = defaultAdmin;
    }

    // --- Profile Section Logic ---
    const profileFirstName = document.getElementById('profile-firstName');
    const profileLastName = document.getElementById('profile-lastName');
    const profileDateOfBirth = document.getElementById('profile-dateOfBirth');
    const profileAboutMe = document.getElementById('profile-aboutMe');
    const profileEmail = document.getElementById('profile-email');
    const profileRole = document.getElementById('profile-role');

    const editProfileForm = document.getElementById('editProfileForm');
    const editFirstName = document.getElementById('edit-firstName');
    const editLastName = document.getElementById('edit-lastName');
    const editAboutMe = document.getElementById('edit-aboutMe');
    const editDateOfBirth = document.getElementById('edit-dateOfBirth');
    const editEmail = document.getElementById('edit-email');
    const editPassword = document.getElementById('edit-password');
    const confirmPassword = document.getElementById('confirm-password');

    function displayProfileInfo() {
        if (loggedInUser) {
            profileFirstName.textContent = loggedInUser.firstName || '-';
            profileLastName.textContent = loggedInUser.lastName || '-';
            profileDateOfBirth.textContent = loggedInUser.dateOfBirth || '-';
            profileAboutMe.textContent = loggedInUser.aboutMe || '-';
            profileEmail.textContent = loggedInUser.email || '-';
            profileRole.textContent = loggedInUser.role || '-';

            // Populate edit form fields
            editFirstName.value = loggedInUser.firstName || '';
            editLastName.value = loggedInUser.lastName || '';
            editAboutMe.value = loggedInUser.aboutMe || '';
            editDateOfBirth.value = loggedInUser.dateOfBirth || '';
            editEmail.value = loggedInUser.email || '';
            editPassword.value = '';
            confirmPassword.value = '';
        }
    }

    editProfileForm.addEventListener('submit', (event) => {
        event.preventDefault();

        const newPassword = editPassword.value;
        const confirmPass = confirmPassword.value;

        if (newPassword && newPassword !== confirmPass) {
            alert('Новий пароль та підтвердження не співпадають!');
            return;
        }

        const updatedUser = {
            ...loggedInUser,
            firstName: editFirstName.value,
            lastName: editLastName.value,
            aboutMe: editAboutMe.value,
            dateOfBirth: editDateOfBirth.value,
            email: editEmail.value,
        };

        if (newPassword) {
            updatedUser.password = newPassword; // In real app, hash this password!
        }

        // Update in global users array
        const userIndex = users.findIndex(u => u.id === loggedInUser.id);
        if (userIndex !== -1) {
            users[userIndex] = updatedUser;
            localStorage.setItem('users', JSON.stringify(users));
        }

        loggedInUser = updatedUser; // Update local loggedInUser object
        localStorage.setItem('loggedInUser', JSON.stringify(loggedInUser));

        displayProfileInfo();
        alert('Профіль успішно оновлено!');
    });

    // --- Create Forms Logic ---

    // Section for creating admin (first set of duplicated IDs)
    const createAdminSection = document.querySelector('#creating-page > section:nth-of-type(1)');
    const newAdminFirstName = createAdminSection.querySelector('#new-user-first');
    const newAdminLastName = createAdminSection.querySelector('#new-user-last');
    const newAdminEmail = createAdminSection.querySelector('#new-user-email');
    const newAdminPassword = createAdminSection.querySelector('#new-user-pass');
    const newAdminDateOfBirth = createAdminSection.querySelector('#new-user-dateOfBirth');
    const createAdminButton = createAdminSection.querySelector('#create-user-button');

    // Section for creating school
    const newSchoolName = document.getElementById('new-school-name');
    const createSchoolButton = document.getElementById('create-school-button');

    // Section for creating class
    const newClassSchoolName = document.getElementById('new-class-school-name');
    const newClassName = document.getElementById('new-class-name');
    const createClassButton = document.getElementById('create-class-button');

    // Section for creating user (second set of duplicated IDs)
    const createUserSection = document.querySelector('#creating-page > section:nth-of-type(4)');
    const newUserFirstName = createUserSection.querySelector('#new-user-first');
    const newUserLastName = createUserSection.querySelector('#new-user-last');
    const newUserEmail = createUserSection.querySelector('#new-user-email');
    const newUserPassword = createUserSection.querySelector('#new-user-pass');
    const newUserSchool = createUserSection.querySelector('#new-user-school');
    const newUserClass = createUserSection.querySelector('#new-user-class');
    const newUserDateOfBirth = createUserSection.querySelector('#new-user-dateOfBirth');
    const newUserRole = createUserSection.querySelector('#new-user-role');
    const createUserButton = createUserSection.querySelector('#create-user-button');

    // Function to clear input fields for admin/user creation
    function clearUserInputs(isAdmin = true) {
        if (isAdmin) {
            newAdminFirstName.value = '';
            newAdminLastName.value = '';
            newAdminEmail.value = '';
            newAdminPassword.value = '';
            newAdminDateOfBirth.value = '';
        } else {
            newUserFirstName.value = '';
            newUserLastName.value = '';
            newUserEmail.value = '';
            newUserPassword.value = '';
            newUserSchool.value = '';
            newUserClass.value = '';
            newUserDateOfBirth.value = '';
            newUserRole.value = '';
        }
    }

    // Create Admin
    createAdminButton.addEventListener('click', () => {
        const firstName = newAdminFirstName.value.trim();
        const lastName = newAdminLastName.value.trim();
        const email = newAdminEmail.value.trim();
        const password = newAdminPassword.value.trim();
        const dateOfBirth = newAdminDateOfBirth.value;

        if (!firstName || !lastName || !email || !password || !dateOfBirth) {
            alert('Будь ласка, заповніть всі поля для створення адміна.');
            return;
        }
        if (users.some(u => u.email === email)) {
            alert('Користувач з таким email вже існує.');
            return;
        }

        const newAdmin = {
            id: `user_${Date.now()}`,
            firstName,
            lastName,
            email,
            password, // In real app, hash this!
            dateOfBirth,
            role: 'ADMIN',
            aboutMe: ''
        };
        users.push(newAdmin);
        localStorage.setItem('users', JSON.stringify(users));
        alert('Нового адміна успішно створено!');
        clearUserInputs(true);
        displayAdminsList(); // Refresh list if on statistics tab
    });

    // Create School
    createSchoolButton.addEventListener('click', () => {
        const schoolName = newSchoolName.value.trim();

        if (!schoolName) {
            alert('Будь ласка, введіть назву школи.');
            return;
        }
        if (schools.some(s => s.name.toLowerCase() === schoolName.toLowerCase())) {
            alert('Школа з такою назвою вже існує.');
            return;
        }

        const newSchool = {
            id: `school_${Date.now()}`,
            name: schoolName
        };
        schools.push(newSchool);
        localStorage.setItem('schools', JSON.stringify(schools));
        alert('Школу успішно створено!');
        newSchoolName.value = '';
        displaySchoolsList(); // Refresh list if on statistics tab
    });

    // Create Class
    createClassButton.addEventListener('click', () => {
        const schoolName = newClassSchoolName.value.trim();
        const className = newClassName.value.trim();

        if (!schoolName || !className) {
            alert('Будь ласка, заповніть всі поля для створення класу.');
            return;
        }

        const existingSchool = schools.find(s => s.name.toLowerCase() === schoolName.toLowerCase());
        if (!existingSchool) {
            alert('Школа з такою назвою не знайдена. Будь ласка, спочатку створіть її.');
            return;
        }
        if (classes.some(c => c.schoolId === existingSchool.id && c.name.toLowerCase() === className.toLowerCase())) {
            alert('Клас з такою назвою вже існує у цій школі.');
            return;
        }

        const newClass = {
            id: `class_${Date.now()}`,
            name: className,
            schoolId: existingSchool.id,
            schoolName: existingSchool.name
        };
        classes.push(newClass);
        localStorage.setItem('classes', JSON.stringify(classes));
        alert('Клас успішно створено!');
        newClassSchoolName.value = '';
        newClassName.value = '';
        displayClassesList(existingSchool.name); // Refresh class list for this school
    });

    // Create User (Teacher/Director)
    createUserButton.addEventListener('click', () => {
        const firstName = newUserFirstName.value.trim();
        const lastName = newUserLastName.value.trim();
        const email = newUserEmail.value.trim();
        const password = newUserPassword.value.trim();
        const schoolName = newUserSchool.value.trim();
        const className = newUserClass.value.trim(); // Only relevant for TEACHER
        const dateOfBirth = newUserDateOfBirth.value;
        const role = newUserRole.value;

        if (!firstName || !lastName || !email || !password || !schoolName || !dateOfBirth || !role) {
            alert('Будь ласка, заповніть всі обов\'язкові поля для створення користувача.');
            return;
        }

        if (users.some(u => u.email === email)) {
            alert('Користувач з таким email вже існує.');
            return;
        }

        const existingSchool = schools.find(s => s.name.toLowerCase() === schoolName.toLowerCase());
        if (!existingSchool) {
            alert('Школа з такою назвою не знайдена. Будь ласка, спочатку створіть її.');
            return;
        }

        let assignedClassId = null;
        if (role === 'TEACHER') {
            if (!className) {
                alert('Будь ласка, вкажіть клас для вчителя.');
                return;
            }
            const existingClass = classes.find(c => c.schoolId === existingSchool.id && c.name.toLowerCase() === className.toLowerCase());
            if (!existingClass) {
                alert('Клас з такою назвою не знайдено у вказаній школі. Будь ласка, спочатку створіть його.');
                return;
            }
            assignedClassId = existingClass.id;
        }

        const newUser = {
            id: `user_${Date.now()}`,
            firstName,
            lastName,
            email,
            password, // In real app, hash this!
            dateOfBirth,
            schoolId: existingSchool.id,
            schoolName: existingSchool.name,
            classId: assignedClassId,
            className: className, // Store class name for easier display
            role,
            aboutMe: ''
        };

        users.push(newUser);
        localStorage.setItem('users', JSON.stringify(users));
        alert(`${role === 'TEACHER' ? 'Вчителя' : 'Директора'} успішно створено!`);
        clearUserInputs(false);
        displayDirectorsList(schoolName); // Refresh directors list
        displayTeachersList(schoolName, className); // Refresh teachers list
    });


    // --- Statistics Section Logic ---
    const adminsList = document.getElementById('admins-list');
    const schoolList = document.getElementById('school-list');
    const directorsList = document.getElementById('directors-list');
    const teachersList = document.getElementById('teachers-list');

    const chooseSchoolForClasses = document.querySelector('#statistics-section section:nth-of-type(3) #choose-school');
    const chooseSchoolForDirectors = document.querySelector('#statistics-section section:nth-of-type(4) #choose-school');
    const chooseSchoolForTeachers = document.querySelector('#statistics-section section:nth-of-type(5) #choose-school');
    const chooseClassForTeachers = document.getElementById('choose-class');


    function displayAdminsList() {
        adminsList.innerHTML = '';
        const admins = users.filter(user => user.role === 'ADMIN');
        if (admins.length === 0) {
            adminsList.innerHTML = '<li>Немає зареєстрованих адмінів.</li>';
            return;
        }
        admins.forEach(admin => {
            const li = document.createElement('li');
            li.textContent = `${admin.firstName} ${admin.lastName} (${admin.email})`;
            adminsList.appendChild(li);
        });
    }

    function displaySchoolsList() {
        schoolList.innerHTML = '';
        if (schools.length === 0) {
            schoolList.innerHTML = '<li>Немає зареєстрованих шкіл.</li>';
            return;
        }
        schools.forEach(school => {
            const li = document.createElement('li');
            li.textContent = school.name;
            schoolList.appendChild(li);
        });
    }

    function displayClassesList(schoolName) {
        const classListSection = document.querySelector('#statistics-section section:nth-of-type(3)');
        let ul = classListSection.querySelector('ul');
        if (!ul) {
            ul = document.createElement('ul');
            classListSection.appendChild(ul);
        }
        ul.innerHTML = '';

        const school = schools.find(s => s.name.toLowerCase() === schoolName.toLowerCase());
        if (!school) {
            ul.innerHTML = '<li>Школа не знайдена.</li>';
            return;
        }

        const schoolClasses = classes.filter(cls => cls.schoolId === school.id);
        if (schoolClasses.length === 0) {
            ul.innerHTML = '<li>Немає класів для цієї школи.</li>';
            return;
        }
        schoolClasses.forEach(cls => {
            const li = document.createElement('li');
            li.textContent = cls.name;
            ul.appendChild(li);
        });
    }

    chooseSchoolForClasses.addEventListener('input', (event) => {
        displayClassesList(event.target.value.trim());
    });

    function displayDirectorsList(schoolName) {
        directorsList.innerHTML = '';
        const school = schools.find(s => s.name.toLowerCase() === schoolName.toLowerCase());
        if (!school) {
            directorsList.innerHTML = '<li>Школа не знайдена.</li>';
            return;
        }

        const schoolDirectors = users.filter(user => user.role === 'DIRECTOR' && user.schoolId === school.id);
        if (schoolDirectors.length === 0) {
            directorsList.innerHTML = '<li>Немає директорів для цієї школи.</li>';
            return;
        }
        schoolDirectors.forEach(director => {
            const li = document.createElement('li');
            li.textContent = `${director.firstName} ${director.lastName} (${director.email})`;
            directorsList.appendChild(li);
        });
    }

    chooseSchoolForDirectors.addEventListener('input', (event) => {
        displayDirectorsList(event.target.value.trim());
    });

    function displayTeachersList(schoolName, className) {
        teachersList.innerHTML = '';
        const school = schools.find(s => s.name.toLowerCase() === schoolName.toLowerCase());
        if (!school) {
            teachersList.innerHTML = '<li>Школа не знайдена.</li>';
            return;
        }

        let filteredTeachers = users.filter(user => user.role === 'TEACHER' && user.schoolId === school.id);

        if (className) {
            const targetClass = classes.find(cls => cls.schoolId === school.id && cls.name.toLowerCase() === className.toLowerCase());
            if (!targetClass) {
                teachersList.innerHTML = '<li>Клас не знайдено у цій школі.</li>';
                return;
            }
            filteredTeachers = filteredTeachers.filter(user => user.classId === targetClass.id);
        }

        if (filteredTeachers.length === 0) {
            teachersList.innerHTML = '<li>Немає вчителів для цієї школи (і класу, якщо вказано).</li>';
            return;
        }
        filteredTeachers.forEach(teacher => {
            const li = document.createElement('li');
            li.textContent = `${teacher.firstName} ${teacher.lastName} (${teacher.email}) - ${teacher.className || 'без класу'}`;
            teachersList.appendChild(li);
        });
    }

    chooseSchoolForTeachers.addEventListener('input', (event) => {
        displayTeachersList(event.target.value.trim(), chooseClassForTeachers.value.trim());
    });

    chooseClassForTeachers.addEventListener('input', (event) => {
        displayTeachersList(chooseSchoolForTeachers.value.trim(), event.target.value.trim());
    });

    // Initial display of data when page loads (if statistics tab is active)
    displayProfileInfo();
    displayAdminsList();
    displaySchoolsList();
    // Classes, directors, teachers lists will only update when their respective input fields are used.
});