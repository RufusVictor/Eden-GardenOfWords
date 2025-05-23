// Initialize Quill editor
const quill = new Quill('#editor', {
    theme: 'snow',
    placeholder: 'Share your thoughts...',
    modules: {
        toolbar: [
            [{ header: [3, false] }],
            ['bold', 'italic', 'underline', 'strike'],
            [{ color: [] }, { background: [] }],
            [{ align: [] }], [{ 'list': 'ordered' }],
            ['link','code-block'],
            ['clean']
        ]
    }
});

// Function to remove images from the editor
function removeImages() {
    const images = quill.root.querySelectorAll('img');
    images.forEach(img => img.remove());
}

// Prevent pasting images
quill.root.addEventListener('paste', function (e) {
    const clipboardData = e.clipboardData || window.clipboardData;
    const items = clipboardData.items;
    for (let i = 0; i < items.length; i++) {
        if (items[i].type.indexOf('image') !== -1) {
            e.preventDefault();
            openModal('Pasting images is disabled.');
            return;
        }
    }
});

// Prevent dragging and dropping images
quill.root.addEventListener('dragover', function (e) {
    e.preventDefault();
});
quill.root.addEventListener('drop', function (e) {
    const clipboardData = e.dataTransfer || e.clipboardData;
    if (clipboardData && clipboardData.files.length) {
        const file = clipboardData.files[0];
        if (file.type.indexOf('image') !== -1) {
            e.preventDefault();
            openModal('Dragging and dropping images is disabled.');
        }
    }
});

// Remove images periodically
removeImages();
quill.on('text-change', function () {
    removeImages();

    // Save content to localStorage on text change
    const htmlContent = quill.root.innerHTML.trim();
    localStorage.setItem('draftContent', htmlContent);
});

// Load cached data on page load
window.addEventListener('load', () => {
    const cachedTitle = localStorage.getItem('draftTitle');
    const cachedContent = localStorage.getItem('draftContent');

    // Populate title input if cached
    const titleInput = document.querySelector('input[name="title"]');
    if (cachedTitle) {
        titleInput.value = cachedTitle;
    }

    // Populate Quill editor if cached
    if (cachedContent) {
        quill.root.innerHTML = cachedContent;
    }

    // Update logout button icon/text
    updateLogoutButton();
});

// Save title to localStorage on input change
document.querySelector('input[name="title"]').addEventListener('input', function () {
    const title = this.value.trim();
    localStorage.setItem('draftTitle', title);
});

// Handle form submission
document.getElementById('post-form').onsubmit = function (e) {
    e.preventDefault();

    const titleInput = this.querySelector('input[name="title"]');
    const title = titleInput.value.trim();
    if (!title) {
        openModal("Title cannot be empty!");
        return;
    }

    let plainText = quill.getText().trim().replace(/\n{2,}/g, '\n');
    let htmlContent = quill.root.innerHTML.trim().replace(/(<p><br><\/p>)+/g, '').replace(/<span class="ql-ui" contenteditable="false"><\/span>/g, '');
    if (!plainText) {
        openModal("Post cannot be empty!");
        return;
    }

    // Store content in hidden input
    document.getElementById('hidden-content').value = htmlContent;

    // Clear cached data after successful submission
    localStorage.removeItem('draftTitle');
    localStorage.removeItem('draftContent');

    // Submit the form
    this.submit();
};

// Update logout button based on screen size
function updateLogoutButton() {
    const logoutButton = document.getElementById("logoutLink");
    if (logoutButton) {
        logoutButton.innerHTML = window.innerWidth <= 768
            ? `<svg id="Layer_1" data-name="Layer 1" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 122.88 122.88"><defs><style>.cls-1{fill:#ef4136;}.cls-1,.cls-2{fill-rule:evenodd;}.cls-2{fill:#ff7c73;}.cls-3{fill:#fff;}</style></defs><title>turn-off</title><path class="cls-1" d="M61.44,0A61.44,61.44,0,1,1,0,61.44,61.44,61.44,0,0,1,61.44,0Z"/><path class="cls-2" d="M61.44,2.25A59.15,59.15,0,0,1,120.59,61.4c0,.77,0,1.54,0,2.3a59.14,59.14,0,0,0-118.2,0c0-.76,0-1.53,0-2.3A59.15,59.15,0,0,1,61.44,2.25Z"/><path class="cls-3" d="M81,44.75a7.08,7.08,0,0,1,10.71-9.27,40,40,0,1,1-60.87.39A7.07,7.07,0,0,1,41.67,45,25.85,25.85,0,1,0,81,44.75ZM68.54,47.92a7.1,7.1,0,1,1-14.2,0V26.74a7.1,7.1,0,1,1,14.2,0V47.92Z"/></svg>`
            : "Logout";
    }
}

function openModal(message) {
    const modal = document.createElement("div");
    modal.classList.add("modal");
    modal.innerHTML = `
        <div class="modal-overlay">
            <div class="modal-content">
                <h2>Message</h2>
                <textarea readonly class="message-textarea">${message}</textarea>
                <button id="close-message-modal">Okay</button>
            </div>
        </div>
    `;
    document.body.appendChild(modal);
    const closeButton = modal.querySelector("#close-message-modal");
    const textarea = modal.querySelector(".message-textarea");
    textarea.focus();
    closeButton.onclick = () => {
        modal.remove();
    };
    textarea.addEventListener("keydown", (event) => {
        if (event.key === "Enter") {
            event.preventDefault();
            closeButton.click();
        }
    });
    const handleEscapeKey = (event) => {
        if (event.key === "Escape") {
            modal.remove();
            document.removeEventListener("keydown", handleEscapeKey);
        }
    };
    document.addEventListener("keydown", handleEscapeKey);
}
// Update logout button on resize
window.addEventListener('resize', () => {
    updateLogoutButton();
});