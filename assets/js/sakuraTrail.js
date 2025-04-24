// Check if the device is mobile or tablet
const isMobileOrTablet = /Mobi|Android|iPhone|iPad|iPod/i.test(navigator.userAgent);

if (isMobileOrTablet) {
    console.log("Cursor trail disabled on mobile/tablet.");
} else {
    const canvas = document.getElementById("magicCanvas");
    const ctx = canvas.getContext("2d");

    // Set canvas dimensions to match the window size
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;

    // Resize canvas on window resize
    window.addEventListener("resize", () => {
        canvas.width = window.innerWidth;
        canvas.height = window.innerHeight;
    });

    // Preload sakura images
    const sakuraImages = [
        new Image(),
        new Image(),
        new Image(),
    ];
    sakuraImages[0].src = "petal1.webp";
    sakuraImages[1].src = "petal2.webp";
    sakuraImages[2].src = "petal3.webp";

    let imagesLoaded = 0;
    sakuraImages.forEach((img) => {
        img.onload = () => {
            imagesLoaded++;
            if (imagesLoaded === sakuraImages.length) {
                animate(); // Start animation once all images are loaded
            }
        };
        img.onerror = () => console.error("Error loading sakura image.");
    });

    // Particle class
    class Particle {
        constructor(x, y) {
            this.x = x;
            this.y = y;
            this.image = sakuraImages[Math.floor(Math.random() * sakuraImages.length)];
            this.size = Math.random() * 30 + 30; // Random size between 30px and 60px
            this.opacity = 1;
            this.scale = Math.random() * 0.4 + 0.4; // Random scale between 0.4x and 0.8x
            this.rotation = Math.random() * 360; // Random initial rotation
            this.speed = {
                x: (Math.random() - 0.5) * 1.5, // Reduced speed for smoother motion
                y: (Math.random() - 0.5) * 1.5,
            };
        }

        draw() {
            ctx.save();
            ctx.translate(this.x, this.y);
            ctx.rotate((this.rotation * Math.PI) / 180);
            ctx.scale(this.scale, this.scale);

            // Draw the sakura image
            ctx.globalAlpha = this.opacity;
            ctx.drawImage(this.image, -this.size / 2, -this.size / 2, this.size, this.size);

            ctx.restore();
        }

        update() {
            this.x += this.speed.x;
            this.y += this.speed.y;
            this.opacity -= 0.008; // Fade out
            this.scale -= 0.003; // Shrink
            this.rotation += 0.5; // Rotate
        }
    }

    let particles = [];
    let isIdle = true;

    function createParticle(x, y) {
        particles.push(new Particle(x, y));
        if (particles.length > 50) particles.shift(); // Limit to 50 particles
    }

    // Smooth cursor tracking with inertia
    let mouseX = 0, mouseY = 0;
    let posX = 0, posY = 0;
    const inertia = 0.15;

    document.addEventListener("mousemove", (e) => {
        isIdle = false;
        mouseX = e.clientX;
        mouseY = e.clientY;
    });

    let frameCount = 0; // Track frames for throttling

    function animate() {
        // Clear the canvas
        ctx.clearRect(0, 0, canvas.width, canvas.height);

        // Smooth follow effect
        posX += (mouseX - posX) * inertia;
        posY += (mouseY - posY) * inertia;

        // Create particles only when moving, and throttle creation
        if (!isIdle && (Math.abs(mouseX - posX) > 1 || Math.abs(mouseY - posY) > 1)) {
            frameCount++;
            if (frameCount % 3 === 0) { // Create a particle every 3 frames
                createParticle(posX, posY);
            }
        }

        // Update and draw particles
        particles.forEach((particle, index) => {
            particle.update();
            particle.draw();

            // Remove faded particles
            if (particle.opacity <= 0 || particle.scale <= 0) {
                particles.splice(index, 1);
            }
        });

        requestAnimationFrame(animate);
    }

    // Detect idle state
    let idleTimer;
    document.addEventListener("mousemove", () => {
        clearTimeout(idleTimer);
        idleTimer = setTimeout(() => isIdle = true, 100);
    });
}