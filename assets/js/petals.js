// Check if the device is mobile or tablet
const isMobileOrTablet = /Mobi|Android|iPhone|iPad|iPod/i.test(navigator.userAgent);

if (isMobileOrTablet) {
    console.log("Cursor trail disabled on mobile/tablet.");
}

// Falling Petals Canvas
const fallingCanvas = document.getElementById("fallingPetalsCanvas");
const fallingCtx = fallingCanvas.getContext("2d");

// Cursor Trail Canvas
const cursorCanvas = document.getElementById("cursorTrailCanvas");
const cursorCtx = cursorCanvas.getContext("2d");

// Scale factor for mobile devices
const scaleFactor = isMobileOrTablet ? 0.5 : 1;

// Resize both canvases on window resize
function resizeCanvas(canvas) {
    canvas.width = window.innerWidth * scaleFactor; // Scale down for mobile
    canvas.height = window.innerHeight * scaleFactor; // Scale down for mobile
    canvas.style.width = `${window.innerWidth}px`; // Maintain visual size
    canvas.style.height = `${window.innerHeight}px`;
}
resizeCanvas(fallingCanvas);
resizeCanvas(cursorCanvas);
window.addEventListener("resize", () => {
    resizeCanvas(fallingCanvas);
    resizeCanvas(cursorCanvas);
});

// Mouse tracking for subtle wind effect
let mouseX = -100, mouseY = -100;

// Update mouseX and mouseY for mouse and touch events
function updateMousePosition(x, y) {
    mouseX = x;
    mouseY = y;
}

window.addEventListener("mousemove", (event) => {
    updateMousePosition(event.clientX, event.clientY);
});

window.addEventListener("touchmove", (event) => {
    const touch = event.touches[0]; // Get the first touch point
    const rect = fallingCanvas.getBoundingClientRect();

    // Check if the touch is within the canvas bounds
    if (
        touch.clientX >= rect.left &&
        touch.clientX <= rect.right &&
        touch.clientY >= rect.top &&
        touch.clientY <= rect.bottom
    ) {
        updateMousePosition(touch.clientX, touch.clientY);
    }
});

// Preload petal images
const petalImages = ["assets/images/petal1.webp", "assets/images/petal2.webp", "assets/images/petal3.webp"].map(src => {
    const img = new Image();
    img.src = src;
    return img;
});

let imagesLoaded = 0;

// Declare the `animate` function before using it
function animate() {
    // Clear both canvases
    fallingCtx.clearRect(0, 0, fallingCanvas.width, fallingCanvas.height);
    cursorCtx.clearRect(0, 0, cursorCanvas.width, cursorCanvas.height);

    // Update and draw falling petals
    for (let petal of fallingPetals) {
        petal.update();
        petal.draw();
    }

    // Update cursor trail if not on mobile/tablet
    if (!isMobileOrTablet) {
        updateCursorTrail();
    }

    requestAnimationFrame(animate);
}

petalImages.forEach((img) => {
    img.onload = () => {
        imagesLoaded++;
        if (imagesLoaded === petalImages.length) {
            animate(); // Start animation after all images are loaded
        }
    };
    img.onerror = () => console.error("Error loading petal image.");
});

// Falling Petals Class
class FallingPetal {
    constructor() {
        this.image = petalImages[Math.floor(Math.random() * petalImages.length)];
        this.reset();
    }

    reset() {
        this.x = Math.random() * fallingCanvas.width;
        this.y = Math.random() * -fallingCanvas.height;
        this.size = Math.random() * 15 + 15; // Base size
        this.size *= scaleFactor; // Scale size for mobile
        this.speed = Math.random() * 0.5 + 0.3;
        this.drift = Math.random() * 0.4 - 0.2;
        this.rotation = Math.random() * 360;
        this.rotationSpeed = Math.random() * 0.2 - 0.1;
    }

    update() {
        // Mouse repelling effect
        const dx = this.x - mouseX;
        const dy = this.y - mouseY;
        const distSq = dx * dx + dy * dy;
        if (distSq < 10000) { // Threshold for interaction
            const distance = Math.sqrt(distSq);
            const repelFactor = (100 - distance) / 100 * 0.2;
            if (distance > 0) {
                this.x += (dx / distance) * repelFactor * 2;
                this.y += (dy / distance) * repelFactor * 2;
            }
        }

        // Natural falling
        this.y += this.speed;
        this.x += this.drift;
        this.rotation += this.rotationSpeed;

        // Reset when out of screen
        if (this.y > fallingCanvas.height) this.reset();
    }

    draw() {
        fallingCtx.save();
        fallingCtx.translate(this.x, this.y);
        fallingCtx.rotate((this.rotation * Math.PI) / 180);
        fallingCtx.drawImage(this.image, -this.size / 2, -this.size / 2, this.size, this.size);
        fallingCtx.restore();
    }
}

const fallingPetals = [];
const petalCount = isMobileOrTablet ? 20 : 40; // Fewer petals for mobile
for (let i = 0; i < petalCount; i++) {
    fallingPetals.push(new FallingPetal());
}

// Cursor Trail Particles Class
if (!isMobileOrTablet) {
    class CursorParticle {
        constructor(x, y) {
            this.image = petalImages[Math.floor(Math.random() * petalImages.length)];
            this.x = x;
            this.y = y;
            this.size = Math.random() * 20 + 20; // Random size
            this.opacity = 1;
            this.scale = Math.random() * 0.4 + 0.4; // Random scale between 0.4x and 0.8x
            this.rotation = Math.random() * 360; // Random initial rotation
            this.speed = {
                x: (Math.random() - 0.5) * 2, // Horizontal speed
                y: (Math.random() - 0.5) * 1.5, // Vertical speed
            };
        }

        draw() {
            cursorCtx.save();
            cursorCtx.globalAlpha = this.opacity;
            cursorCtx.translate(this.x, this.y);
            cursorCtx.rotate((this.rotation * Math.PI) / 180);
            cursorCtx.scale(this.scale, this.scale);
            cursorCtx.drawImage(this.image, -this.size / 2, -this.size / 2, this.size, this.size);
            cursorCtx.restore();
        }

        update() {
            this.x += this.speed.x;
            this.y += this.speed.y;
            this.opacity -= 0.004; // Fade-out speed
            this.scale -= 0.004;  // Shrinking speed
            this.rotation += 1; // Rotation speed
        }
    }

    const cursorParticles = [];
    let posX = 0, posY = 0;
    const inertia = 0.35;
    let frameCount = 0;
    let isIdle = true;
    let isOverForm = false; // Track whether the cursor is over the form

    document.addEventListener("mousemove", (e) => {
        isIdle = false;
        mouseX = e.clientX;
        mouseY = e.clientY;
    });

    function createCursorParticle(x, y) {
        if (!isOverForm) { // Only create particles if not over the form
            cursorParticles.push(new CursorParticle(x, y));
            if (cursorParticles.length > 40) cursorParticles.shift(); // Limit to 40 particles
        }
    }

    function updateCursorTrail() {
        // Smooth follow effect
        posX += (mouseX - posX) * inertia;
        posY += (mouseY - posY) * inertia;

        // Create particles only when moving, and throttle creation
        if (!isIdle && !isOverForm && (Math.abs(mouseX - posX) > 1 || Math.abs(mouseY - posY) > 1)) {
            frameCount++;
            if (frameCount % 4 === 0) { 
                createCursorParticle(posX, posY);
            }
        }

        // Update and draw particles
        cursorParticles.forEach((particle, index) => {
            particle.update();
            particle.draw();

            // Remove faded particles
            if (particle.opacity <= 0 || particle.scale <= 0) {
                cursorParticles.splice(index, 1);
            }
        });
    }

    // Idle detection
    let idleTimer;
    document.addEventListener("mousemove", () => {
        clearTimeout(idleTimer);
        idleTimer = setTimeout(() => isIdle = true, 100);
    });

    // Detect when the cursor is over the form container
    const formContainer = document.querySelector(".form-container");
    if (formContainer) {
        formContainer.addEventListener("mouseenter", () => {
            isOverForm = true; // Pause particle generation
        });

        formContainer.addEventListener("mouseleave", () => {
            isOverForm = false; // Resume particle generation
        });
    }
}