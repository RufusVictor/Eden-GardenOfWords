function updatePostButton() {
    const postButton = document.querySelector('.new-post-btn');
    if (postButton) {
        postButton.innerHTML = window.innerWidth <= 768
            ? `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 511.99"><path fill="#00AB42" d="M256 0c70.68 0 134.69 28.66 181.01 74.98C483.35 121.31 512 185.31 512 255.99c0 70.68-28.66 134.69-74.99 181.02-46.32 46.32-110.33 74.98-181.01 74.98-70.68 0-134.69-28.66-181.02-74.98C28.66 390.68 0 326.67 0 255.99S28.65 121.31 74.99 74.98C121.31 28.66 185.32 0 256 0zm116.73 236.15v39.69c0 9.39-7.72 17.12-17.11 17.12h-62.66v62.66c0 9.4-7.71 17.11-17.11 17.11h-39.7c-9.4 0-17.11-7.69-17.11-17.11v-62.66h-62.66c-9.39 0-17.11-7.7-17.11-17.12v-39.69c0-9.41 7.69-17.11 17.11-17.11h62.66v-62.67c0-9.41 7.7-17.11 17.11-17.11h39.7c9.41 0 17.11 7.71 17.11 17.11v62.67h62.66c9.42 0 17.11 7.76 17.11 17.11zm37.32-134.21c-39.41-39.41-93.89-63.8-154.05-63.8-60.16 0-114.64 24.39-154.05 63.8-39.42 39.42-63.81 93.89-63.81 154.05 0 60.16 24.39 114.64 63.8 154.06 39.42 39.41 93.9 63.8 154.06 63.8s114.64-24.39 154.05-63.8c39.42-39.42 63.81-93.9 63.81-154.06s-24.39-114.63-63.81-154.05z"/></svg>`
            : "Create a Post";
    }
}

function updateLogoutButton() {
    const logoutButton = document.getElementById("logoutLink");
    if (logoutButton) {
        logoutButton.innerHTML = window.innerWidth <= 768
            ? `<svg id="Layer_1" data-name="Layer 1" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 122.88 122.88"><defs><style>.cls-1{fill:#ef4136;}.cls-1,.cls-2{fill-rule:evenodd;}.cls-2{fill:#ff7c73;}.cls-3{fill:#fff;}</style></defs><title>turn-off</title><path class="cls-1" d="M61.44,0A61.44,61.44,0,1,1,0,61.44,61.44,61.44,0,0,1,61.44,0Z"/><path class="cls-2" d="M61.44,2.25A59.15,59.15,0,0,1,120.59,61.4c0,.77,0,1.54,0,2.3a59.14,59.14,0,0,0-118.2,0c0-.76,0-1.53,0-2.3A59.15,59.15,0,0,1,61.44,2.25Z"/><path class="cls-3" d="M81,44.75a7.08,7.08,0,0,1,10.71-9.27,40,40,0,1,1-60.87.39A7.07,7.07,0,0,1,41.67,45,25.85,25.85,0,1,0,81,44.75ZM68.54,47.92a7.1,7.1,0,1,1-14.2,0V26.74a7.1,7.1,0,1,1,14.2,0V47.92Z"/></svg>`
            : "Logout";
    }
}
function updateSearchPlaceholder() {
    const searchInput = document.getElementById("search-input");
    if (!searchInput) return;
    if (window.matchMedia("(max-width: 440px)").matches && window.matchMedia("(min-width: 390px)").matches) {
        searchInput.placeholder = "Search Posts Here!";
    } else if (window.matchMedia("(max-width: 390px)").matches) {
        searchInput.placeholder = "Search Bar";
    } else {
        searchInput.placeholder = "What are you looking for in Eden?";
    }
}

// Helper function to create a post element
function createPostElement(post) {
    let newPostDiv = document.createElement("div");
    newPostDiv.classList.add("post");
    newPostDiv.setAttribute("data-postid", post.postId);
    newPostDiv.innerHTML = `
        <div class="postNav">${post.isAuthor ? `
            <button class="edit-btn" data-postid="${post.postId}">
                            <svg width="27px" height="24px" viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg" version="1.1">

<title>Edit</title><path style="fill:#ffffff;stroke:none;" d="m 65,3 0,19 19,0 z"/>
  <path style="fill:#EBEBDA;stroke:#777777;stroke-width:2;" d="m 65,3 0,19 19,0 0,74 -72,0 0,-93 53,0 19,19"/>
  <path style="fill:none;stroke:#444444;stroke-width:2px;" d="m 21,26 54,0 -54,0 z m 0,9 54,0 -54,0 z m 0,9 54,0 -54,0 z m 0,8 23,0 -23,0 z m 0,10 54,0 -54,0 z"/>
  <path style="fill:#CF9100;stroke:#333333;stroke-width:2;" d="M 25,68 85,5 c 0,0 5,1 8,4 3,3 4,8 4,8 L 38,80 22,84 z"/>
  <path style="fill:#eeeeee" d="M 85,9 28,69 30,71 87,11 z"/>
  <path style="fill:#333333" d="m 22,84 7,-2 -5,-5 z"/>
</svg>
                        </button>
            <button class="delete-btn" data-postid="${post.postId}">
                              <svg version="1.1" id="Layer_1" xmlns:sketch="http://www.bohemiancoding.com/sketch/ns"
	 xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"  width="27px" height="24px"
	 viewBox="0 0 64 64" enable-background="new 0 0 64 64" xml:space="preserve">
<title>Delete</title>
<g id="Page-1" sketch:type="MSPage">
	<g id="Button-circle-delete" transform="translate(1.000000, 1.000000)" sketch:type="MSLayerGroup">
		<circle id="Oval" sketch:type="MSShapeGroup" fill="none" stroke="#6B6C6E" stroke-width="2" cx="31" cy="31" r="31">
		</circle>
		<path id="Shape" sketch:type="MSShapeGroup" fill="none" stroke="#6B6C6E" stroke-width="2" stroke-linejoin="round" d="
			M43.5,38.1L36.4,31l7.1-7.1l-5.7-5.6l-7.1,7l-7-7L18,23.9l7.1,7.1L18,38.1l5.7,5.6l7-7l7.1,7L43.5,38.1z"/>
		<path id="Shape_1_" sketch:type="MSShapeGroup" fill="none" stroke="#6B6C6E" stroke-width="2" d="M31,57C16.6,57,5,45.4,5,31"/>
		<path id="Shape_2_" sketch:type="MSShapeGroup" fill="none" stroke="#6B6C6E" stroke-width="2" d="M31,5c14.4,0,26,11.6,26,26"/>
	</g>
</g>
</svg>
                        </button>
        ` : ''}</div>
        <h2>${post.title}</h2>
        <div class="post-content">${post.content}</div>
        <small>Posted by: ${post.username} | ${post.timestamp}</small>
        <div class="post-actions">
            <button class="like-btn ${post.liked ? "liked" : ""}" data-postid="${post.postId}">
                ❤️ ${post.likes}
            </button>
            <button class="view-comments-btn" data-postid="${post.postId}">
                💬 Comments
            </button>
            <button class="summarize-btn" data-postid="${post.postId}">
                📖 Summarize
            </button>
        </div>
        <div class="comments-section" id="comments-${post.postId}" style="display: none;">
            <div class="comments-list"></div>
            <button class="load-more-btn" data-postid="${post.postId}">
                Check for New Comments
            </button>
            <div class="new-comment">
                <input type="text" class="comment-input" placeholder="Write a comment..." />
                <button class="add-comment-btn" data-postid="${post.postId}">
                    ✚
                </button>
            </div>
        </div>
    `;
    return newPostDiv;
}

let currentPage = 1;
const pageSize = 10;
let currentSortOption = "desc";
let currentQuery = "";

function fetchPosts(page = currentPage, sortOption = currentSortOption, isUserInitiated = true) {
    fetch(`/Eden/getPosts?page=${page}&pageSize=${pageSize}&sort=${sortOption}`)
        .then(response => {
            if (response.status === 429) {
                window.location.href = "code429.html";
            } else if (!response.ok) {
                throw new Error("Request failed");
            }
            return response.json();
        })
        .then(data => {
            const totalPages = Math.ceil(data.totalPosts / pageSize);
            if (page > totalPages) {
                currentPage = totalPages;
                updateUrlForPagination(currentSortOption, currentPage, currentQuery, isUserInitiated);
                fetchPosts(currentPage, currentSortOption, isUserInitiated);
                return;
            }
            const paginationControls = document.getElementById("pagination-controls");
            if (paginationControls) {
                paginationControls.style.display = "";
            }
            const sortDropdown = document.getElementById("sort-by");
            if (sortDropdown) {
                sortDropdown.disabled = false;
            }
            let postsContainer = document.getElementById("posts");
            if (!postsContainer) return;
            postsContainer.innerHTML = "";
            if (data.posts.length > 0) {
                const fragment = document.createDocumentFragment(); // Document fragment
                data.posts.forEach(post => {
                    let postDiv = document.querySelector(`.post[data-postid="${post.postId}"]`);
                    if (!postDiv) {
                        let newPostDiv = createPostElement(post);
                        fragment.appendChild(newPostDiv);
                    }
                });
                postsContainer.appendChild(fragment);
            }
            updatePaginationControls(data.totalPosts);
            const postCountMessage = document.getElementById("post-count-message");
            if (postCountMessage) {
                const startPost = (currentPage - 1) * pageSize + 1;
                const endPost = Math.min(currentPage * pageSize, data.totalPosts);
                postCountMessage.textContent = `Showing ${startPost}-${endPost} of ${data.totalPosts} total posts`;
            }
            if (isUserInitiated) {
                updateUrlForPagination(currentSortOption, currentPage, currentQuery);
            }
        })
        .catch(error => {
            console.error("Error fetching posts:", error);
        });
}
document.getElementById("posts")?.addEventListener("click", event => {
    const target = event.target.closest('[data-postid], .edit-comment-btn, .delete-comment-btn');
    if (!target) return;

    // Handle Post Actions
    const postId = target.getAttribute("data-postid");
    if (postId) {
        if (target.classList.contains("like-btn")) {
            handleLike(postId, target);
        } else if (target.classList.contains("view-comments-btn")) {
            toggleCommentsSection(postId);
        } else if (target.classList.contains("add-comment-btn")) {
            const inputField = target.closest(".new-comment")?.querySelector(".comment-input");
            if (inputField) addComment(postId, inputField);
        } else if (target.classList.contains("load-more-btn")) {
            loadComments(postId, true);
        } else if (target.classList.contains("edit-btn")) {
            handleEditPost(postId);
        } else if (target.classList.contains("delete-btn")) {
            handleDeletePost(postId);
        } else if (target.classList.contains("summarize-btn")) {
            handleSummarize(postId);
        }
    }

    // Handle Comment Actions
    const commentId = target.getAttribute("data-commentid");
    if (commentId) {
        if (target.classList.contains("edit-comment-btn")) {
            openEditCommentModal(commentId);
        } else if (target.classList.contains("delete-comment-btn")) {
            deleteComment(commentId);
        }
    }
});

document.getElementById("posts")?.addEventListener("keydown", event => {
    if (event.target.classList.contains("comment-input") && event.key === "Enter") {
        event.preventDefault();
        const postId = event.target.closest(".new-comment")?.querySelector(".add-comment-btn")?.getAttribute("data-postid");
        if (postId) {
            addComment(postId, event.target);
        }
    }
});

// Helper Functions for Post Actions
function handleEditPost(postId) {
    if (!postId || isNaN(postId)) {
        console.error("Invalid postId:", postId);
        openModal("An error occurred while processing your request. Please try again.");
        return;
    }

    fetch(`/Eden/getPostDetails/${postId}`)
        .then(response => {
            if (response.status === 429) {
                window.location.href = "code429.html";
            } else if (!response.ok) {
                throw new Error("Failed to fetch post details.");
            }
            return response.json();
        })
        .then(post => {
            openEditModal(post, postId);
        })
        .catch(error => {
            console.error("Error fetching post details:", error);
            openModal("An error occurred while fetching post details.");
        });
}

function handleDeletePost(postId) {
    if (!postId || isNaN(postId)) {
        console.error("Invalid postId:", postId);
        openModal("An error occurred while processing your request. Please try again.");
        return;
    }
    openConfirmModal("Are you sure you want to delete this post?")
        .then((isConfirmed) => {
            if (!isConfirmed) {
                return;
            }
            fetch(`/Eden/deletePost/${postId}`, { method: "DELETE" })
                .then(response => {
                    if (response.status === 204) {
                        document.querySelector(`.post[data-postid="${postId}"]`)?.remove();
                    } else if (response.status === 403) {
                        return response.json();
                    } else if (response.status === 429) {
                        window.location.href = "code429.html";
                    } else {
                        throw new Error("Failed to delete the post.");
                    }
                })
                .then(data => {
                    if (data && data.redirectUrl) {
                        window.location.href = data.redirectUrl;
                    }
                })
                .catch(error => {
                    console.error("Error deleting post:", error);
                    openModal("An error occurred while deleting the post.");
                });
        });

}
function handleSummarize(postId) {
    let loadingSpinner = null;
    let summarizeButton = null;

    try {
        const postDiv = document.querySelector(`.post[data-postid="${postId}"]`);
        if (!postDiv) {
            throw new Error("Post not found.");
        }
        const contentDiv = postDiv.querySelector(".post-content");
        if (!contentDiv) {
            throw new Error("Post content not found.");
        }
        const tempContainer = document.createElement("div");
        tempContainer.style.display = "none";
        document.body.appendChild(tempContainer);

        const quill = new Quill(tempContainer);
        quill.clipboard.dangerouslyPasteHTML(contentDiv.innerHTML);
        const plainText = quill.getText().trim();
        document.body.removeChild(tempContainer);

        if (plainText.length < 600) {
            openModal("The post content is too short to summarize. Please try again with a longer post.");
            return;
        }

        if (plainText.length > 4300) {
            openModal("The post content is too long to summarize. Please try again with a shorter post.");
            return;
        }
        const cachedSummaries = getCachedSummaries();
        const cachedEntry = cachedSummaries.find(entry => entry.plainText === plainText);
        if (cachedEntry) {
            console.log("Using cached summary for the plain text.");
            openSummaryModal(cachedEntry.summary);
            return;
        }
        summarizeButton = document.querySelector(`.summarize-btn[data-postid="${postId}"]`);
        if (summarizeButton) {
            summarizeButton.disabled = true;
        }
        loadingSpinner = createCustomLoadingSpinner();
        document.body.appendChild(loadingSpinner);
        fetch('/Eden/summarizePost', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: `plainText=${encodeURIComponent(plainText)}`
        })
            .then(response => {
                if (response.status === 429) {
                    window.location.href = "code429.html";
                } else if (!response.ok) {
                    throw new Error("Failed to summarize the post.");
                }
                return response.json();
            })
            .then(data => {
                if (!Array.isArray(data) || data.length === 0 || !data[0].summary_text) {
                    if (data.error) {
                        throw new Error(data.error);
                    } else {
                        throw new Error("Invalid response from the server.");
                    }
                }

                const summary = data[0].summary_text;
                addEntryToCache(plainText, summary);
                openSummaryModal(summary);
            })
            .catch(error => {
                console.error("Error summarizing post:", error);
                openModal("An error occurred while summarizing the post.");
            })
            .finally(() => {
                if (loadingSpinner && loadingSpinner.parentNode) {
                    document.body.removeChild(loadingSpinner);
                }
                if (summarizeButton) {
                    summarizeButton.disabled = false;
                }
            });
    } catch (error) {
        console.error("Error in handleSummarize:", error);
        openModal("An unexpected error occurred. Please try again later.");
        if (loadingSpinner && loadingSpinner.parentNode) {
            document.body.removeChild(loadingSpinner);
        }
        if (summarizeButton) {
            summarizeButton.disabled = false;
        }
    }
}
function createCustomLoadingSpinner() {
    const loadingOverlay = document.createElement("div");
    loadingOverlay.className = "loading-overlay";
    const ring = document.createElement("div");
    ring.className = "ring";
    ring.textContent = "Loading";
    const spanTag = document.createElement("span");
    spanTag.className = "spinner";
    ring.appendChild(spanTag);
    loadingOverlay.appendChild(ring);
    return loadingOverlay;
}

function getCachedSummaries() {
    const cachedSummaries = localStorage.getItem("cachedSummaries");
    return cachedSummaries ? JSON.parse(cachedSummaries) : [];
}
function addEntryToCache(plainText, summary) {
    let cachedSummaries = getCachedSummaries();
    const newEntry = {
        plainText: plainText,
        summary: summary,
        entryAddedTime: Date.now()
    };
    cachedSummaries.push(newEntry);
    cachedSummaries.sort((a, b) => b.entryAddedTime - a.entryAddedTime);
    if (cachedSummaries.length > 20) {
        cachedSummaries = cachedSummaries.slice(0, 20);
    }
    localStorage.setItem("cachedSummaries", JSON.stringify(cachedSummaries));
}

function openSummaryModal(summary) {
    const modal = document.createElement("div");
    modal.classList.add("modal");
    modal.innerHTML = `
        <div class="modal-overlay">
            <div class="modal-content">
                <h2>Summary</h2>
                <textarea readonly class="summary-textarea">${summary}</textarea>
                <button id="close-summary-modal">Okay</button>
            </div>
        </div>
    `;
    document.body.appendChild(modal);
    const closeButton = modal.querySelector("#close-summary-modal");
    const textarea = modal.querySelector(".summary-textarea");
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

function openConfirmModal(message) {
    return new Promise((resolve) => {
        const modal = document.createElement("div");
        modal.classList.add("modal");
        modal.innerHTML = `
            <div class="modal-overlay">
                <div class="modal-content">
                    <h2>Confirm Action</h2>
                    <textarea readonly class="message-textarea">${message}</textarea>
                    <div class="modal-actions">
                        <button id="confirm-yes">Yes</button>
                        <button id="confirm-no">No</button>
                    </div>
                </div>
            </div>
        `;
        document.body.appendChild(modal);
        const yesButton = modal.querySelector("#confirm-yes");
        const noButton = modal.querySelector("#confirm-no");
        yesButton.focus();
        yesButton.onclick = () => {
            modal.remove();
            resolve(true);
        };
        noButton.onclick = () => {
            modal.remove();
            resolve(false);
        };
        const handleEscapeKey = (event) => {
            if (event.key === "Escape") {
                modal.remove();
                resolve(false);
                document.removeEventListener("keydown", handleEscapeKey);
            }
        };
        document.addEventListener("keydown", handleEscapeKey);
    });
}

function handleLike(postId, button) {
    fetch(`/Eden/likePost?postId=${postId}`, { method: "POST" })
        .then(response => {
            if (response.status === 429) {
                window.location.href = "code429.html";
            } else if (!response.ok) {
                throw new Error("Failed to fetch post details.");
            }
            return response.json();
        })
        .then(data => {
            if (data.success) {
                button.innerHTML = `❤️ ${data.likeCount}`;
                button.classList.toggle("liked", data.liked);
            }
        })
        .catch(error => console.error("Error liking post:", error));
}

function toggleCommentsSection(postId) {
    const commentsSection = document.getElementById(`comments-${postId}`);
    if (commentsSection.style.display === "none") {
        commentsSection.style.display = "block";
        loadComments(postId);
    } else {
        commentsSection.style.display = "none";
    }
}

function addComment(postId, triggerElement) {
    const inputField = triggerElement.closest(".new-comment")?.querySelector(".comment-input");
    if (!inputField) {
        console.error("Input field not found for post ID:", postId);
        return;
    }
    const content = inputField.value.trim();
    if (!content) {
        return;
    }
    fetch(`/Eden/commentPost`, {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: `postId=${postId}&comment=${encodeURIComponent(content)}`
    })
        .then(response => {
            if (response.status === 429) {
                window.location.href = "code429.html";
            } else if (!response.ok) {
                throw new Error("Failed to fetch post details.");
            }
            return response.text();
        })
        .then(status => {
            if (status === "success") {
                inputField.value = "";
                loadComments(postId, true);
            } else {
                console.error("Failed to add comment. Server returned status:", status);
            }
        })
        .catch(error => console.error("Error adding comment:", error));
}

function loadComments(postId, loadMore = false) {
    let commentsList = document.querySelector(`#comments-${postId} .comments-list`);
    if (!commentsList) return;

    let existingCommentIds = new Set();
    commentsList.querySelectorAll("[data-commentid]").forEach(comment => {
        existingCommentIds.add(comment.getAttribute("data-commentid"));
    });

    let offset = loadMore ? commentsList.children.length : 0;
    if (!loadMore) commentsList.innerHTML = "";

    fetch(`/Eden/getComments?postId=${postId}&offset=${offset}`)
        .then(response => {
            if (response.status === 429) {
                window.location.href = "code429.html";
            } else if (!response.ok) {
                throw new Error("Failed to fetch post details.");
            }
            return response.json();
        })
        .then(comments => {
            const fragment = document.createDocumentFragment(); // Use a document fragment
            let newCommentsAdded = false;

            comments.forEach(comment => {
                if (existingCommentIds.has(comment.commentId)) return;

                let commentElement = document.createElement("div");
                commentElement.classList.add("comment");
                commentElement.setAttribute("data-commentid", comment.commentId);
                commentElement.innerHTML = `
                    <div class="comment-header">
                        <span class="comment-author">${comment.username}</span>
                        <span class="comment-timestamp">${comment.timestamp}</span>
                    </div>
                    <div class="comment-content"><span class="comment-content-text">${comment.comment}</span> ${comment.isAuthor ? `
                        <div class="comment-actions">
                            <button class="edit-comment-btn" data-commentid="${comment.commentId}">
                                <svg width="16px" height="16px" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04a.996.996 0 000-1.41l-2.34-2.34a.996.996 0 00-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"/>
                                </svg>
                            </button>
                            <button class="delete-comment-btn" data-commentid="${comment.commentId}">
                                <svg width="16px" height="16px" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/>
                                </svg>
                            </button>
                        </div>
                    ` : ""}</div>
                `;
                fragment.appendChild(commentElement); // Add to fragment
                newCommentsAdded = true;
            });

            commentsList.appendChild(fragment); // Append all comments at once

            if (!newCommentsAdded && loadMore) {
                openModal("No new comments to show. Try again later!");
            }
        })
        .catch(error => console.error("Error fetching comments:", error));
}

// Search bar logic
let debounceTimer;
const searchInput = document.getElementById("search-input");
const searchButton = document.getElementById("search-button");
// Check if both elements exist before proceeding to avoid error on other pages
if (searchInput && searchButton) {
    searchInput.addEventListener("input", event => {
        const query = event.target.value.trim();
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(() => {
            if (query) {
                currentPage = 1;
                currentQuery = query;
                fetchPostsBySearch(query, currentSortOption, currentPage);
            } else {
                currentPage = 1;
                currentQuery = "";
                fetchPosts(currentPage, currentSortOption);
                updateUrlForPagination(currentSortOption, currentPage, "");
            }
        }, 300);
    });

    searchInput.addEventListener("keypress", event => {
        if (event.key === "Enter") {
            searchButton.click();
        }
    });

    searchButton.addEventListener("click", () => {
        const query = searchInput.value.trim();
        if (query) {
            currentPage = 1;
            currentQuery = query;
            fetchPostsBySearch(query, currentSortOption, currentPage);
        } else {
            openModal("Please enter a search term.");
        }
    });
}
window.addEventListener('load', () => {
    parseQueryParams();
    updatePostButton();
    updateLogoutButton();
    updateSearchPlaceholder();
    initializeSortingDropdown();

    history.replaceState(
        { page: currentPage, sortOption: currentSortOption, query: currentQuery },
        "",
        window.location.href
    );

    if (currentQuery) {
        fetchPostsBySearch(currentQuery, currentSortOption, currentPage, false);
    } else {
        fetchPosts(currentPage, currentSortOption, false);
    }
});

window.addEventListener('resize', () => {
    updatePostButton();
    updateLogoutButton();
    updateSearchPlaceholder();
});

setInterval(() => {
    document.querySelectorAll(".post").forEach(postDiv => {
        const postId = postDiv.getAttribute("data-postid");
        const likeBtn = postDiv.querySelector(".like-btn");

        if (postId && likeBtn) {
            fetch(`/Eden/getLikeCount?postId=${postId}`)
                .then(response => {
                    if (response.status === 429) {
                        window.location.href = "code429.html";
                    } else if (!response.ok) {
                        throw new Error("Failed to fetch post details.");
                    }
                    return response.json();
                })
                .then(data => {
                    if (data.success) {
                        likeBtn.innerHTML = `❤️ ${data.likeCount}`;
                        likeBtn.classList.toggle("liked", data.liked);
                    }
                })
                .catch(error => console.error(`Error fetching like count for post ${postId}:`, error));
        }
    });
}, 60000);
function updatePaginationControls(totalPosts, query = "", sortOption = "") {
    const totalPages = Math.ceil(totalPosts / pageSize);
    const paginationControls = document.getElementById("pagination-controls");
    if (!paginationControls) return;

    // Clear existing buttons
    paginationControls.innerHTML = "";

    // Add "Previous" button
    const prevButton = document.createElement("button");
    prevButton.textContent = "Previous";
    prevButton.disabled = currentPage === 1 || totalPosts === 0;
    prevButton.onclick = () => {
        if (currentPage > 1) {
            currentPage--;
            if (query) {
                fetchPostsBySearch(query, sortOption, currentPage, true);
            } else {
                fetchPosts(currentPage, currentSortOption, true);
            }
            setTimeout(() => {
                let navbar = document.querySelector(".navbar");
                if (navbar) {
                    navbar.scrollIntoView({ behavior: "smooth", block: "start" });
                }
            }, 100);
        }
    };
    paginationControls.appendChild(prevButton);

    // Add "Next" button
    const nextButton = document.createElement("button");
    nextButton.textContent = "Next";
    nextButton.disabled = currentPage === totalPages || totalPosts === 0;
    nextButton.onclick = () => {
        if (currentPage < totalPages) {
            currentPage++;
            if (query) {
                fetchPostsBySearch(query, sortOption, currentPage, true);
            } else {
                fetchPosts(currentPage, currentSortOption, true);
            }
            setTimeout(() => {
                let navbar = document.querySelector(".navbar");
                if (navbar) {
                    navbar.scrollIntoView({ behavior: "smooth", block: "start" });
                }
            }, 100);
        }
    };
    paginationControls.appendChild(nextButton);
}

function updateUIForEmptyResults() {
    const postsContainer = document.getElementById("posts");
    if (postsContainer) {
        postsContainer.innerHTML = "";
    }
    const postCountMessage = document.getElementById("post-count-message");
    if (postCountMessage) {
        postCountMessage.textContent = "No search results found.";
    }
    const paginationControls = document.getElementById("pagination-controls");
    if (paginationControls) {
        paginationControls.style.display = "none";
    }
    const sortDropdown = document.getElementById("sort-by");
    if (sortDropdown) {
        sortDropdown.disabled = true;
    }
}

function fetchPostsBySearch(query, sortOption = currentSortOption, page = currentPage, isUserInitiated = true) {
    fetch(`/Eden/searchPosts?query=${encodeURIComponent(query)}&sort=${sortOption}&page=${page}&pageSize=${pageSize}`)
        .then(response => {
            if (response.status === 429) {
                window.location.href = "code429.html";
            } else if (!response.ok) {
                throw new Error("Failed to fetch search results.");
            }
            return response.json();
        })
        .then(data => {
            const totalPages = Math.ceil(data.totalPosts / pageSize);
            if (data.totalPosts === 0) {
                currentPage = 1;
                updateUrlForPagination(sortOption, currentPage, query, isUserInitiated);
                updateUIForEmptyResults();
                return;
            }
            if (page > totalPages) {
                currentPage = totalPages;
                updateUrlForPagination(sortOption, currentPage, query, isUserInitiated);
                fetchPostsBySearch(query, sortOption, currentPage, false);
                return;
            }
            const paginationControls = document.getElementById("pagination-controls");
            if (paginationControls) {
                paginationControls.style.display = "";
            }
            const sortDropdown = document.getElementById("sort-by");
            if (sortDropdown) {
                sortDropdown.disabled = false;
            }
            let postsContainer = document.getElementById("posts");
            if (!postsContainer) return;
            postsContainer.innerHTML = "";
            if (data.posts.length > 0) {
                const fragment = document.createDocumentFragment();
                data.posts.forEach(post => {
                    let postDiv = document.querySelector(`.post[data-postid="${post.postId}"]`);
                    if (!postDiv) {
                        let newPostDiv = createPostElement(post);
                        fragment.appendChild(newPostDiv);
                    }
                });
                postsContainer.appendChild(fragment);
            }
            updatePaginationControls(data.totalPosts, query, sortOption);
            const postCountMessage = document.getElementById("post-count-message");
            if (postCountMessage) {
                if (data.totalPosts === 0) {
                    postCountMessage.textContent = "No search results found.";
                } else {
                    const startPost = (currentPage - 1) * pageSize + 1;
                    const endPost = Math.min(currentPage * pageSize, data.totalPosts);
                    postCountMessage.textContent = `Showing ${startPost}-${endPost} of ${data.totalPosts} search results`;
                }
            }
            if (isUserInitiated) {
                updateUrlForPagination(sortOption, currentPage, query);
            }
        })
        .catch(error => {
            console.error("Error fetching search results:", error);
            openModal("An error occurred while fetching search results. Please try again.");
        });
}

let isNavigatingViaPopstate = false;

function updateUrlForPagination(sortOption, page, query, isUserInitiated = true) {
    const urlParams = new URLSearchParams(window.location.search);
    urlParams.set("page", page);
    urlParams.set("sort", sortOption);

    if (query) {
        urlParams.set("query", query);
    } else {
        urlParams.delete("query");
    }

    const newUrl = `${window.location.pathname}?${urlParams.toString()}`;

    if (isUserInitiated) {
        history.pushState({ page, sortOption, query }, "", newUrl);
    } else {
        history.replaceState({ page, sortOption, query }, "", newUrl);
    }
}

function parseQueryParams() {
    const urlParams = new URLSearchParams(window.location.search);
    let parsedPage = parseInt(urlParams.get("page")) || 1;
    currentPage = Math.max(1, parsedPage);

    const urlSortOption = urlParams.get("sort");
    const savedSortOption = getSortingPreference();

    if (isValidSortOption(urlSortOption)) {
        currentSortOption = urlSortOption;
    } else {
        currentSortOption = isValidSortOption(savedSortOption) ? savedSortOption : "desc";
    }
    updateUrlForPagination(currentSortOption, currentPage, currentQuery, false);
    currentQuery = urlParams.get("query") || "";
}

window.addEventListener("popstate", (event) => {
    isNavigatingViaPopstate = true;
    let page, sortOption, query;
    if (event.state) {
        page = event.state.page || 1;
        sortOption = event.state.sortOption || "desc";
        query = event.state.query || "";
    } else {
        const urlParams = new URLSearchParams(window.location.search);
        page = parseInt(urlParams.get("page")) || 1;
        sortOption = urlParams.get("sort") || "desc";
        query = urlParams.get("query") || "";
    }

    currentPage = Math.max(1, page);
    const savedSortOption = getSortingPreference();
    currentSortOption = isValidSortOption(sortOption)
        ? sortOption
        : isValidSortOption(savedSortOption)
            ? savedSortOption
            : "desc";
    currentQuery = query;

    if (currentQuery) {
        fetchPostsBySearch(currentQuery, currentSortOption, currentPage, false);
    } else {
        fetchPosts(currentPage, currentSortOption, false);
    }

    const sortDropdown = document.getElementById("sort-by");
    if (sortDropdown) {
        sortDropdown.value = currentSortOption;
    }
    isNavigatingViaPopstate = false;
});

function isValidSortOption(sortOption) {
    const validOptions = ["asc", "desc", "likes_desc"];
    return validOptions.includes(sortOption);
}

function initializeSortingDropdown() {
    const sortDropdown = document.getElementById("sort-by");
    if (!sortDropdown) return;

    const savedSortOption = getSortingPreference();
    const urlSortOption = new URLSearchParams(window.location.search).get("sort");

    if (!isValidSortOption(urlSortOption)) {
        currentSortOption = isValidSortOption(savedSortOption) ? savedSortOption : "desc";
        updateUrlForPagination(currentSortOption, currentPage, currentQuery);
    } else {
        currentSortOption = urlSortOption;
    }

    sortDropdown.value = currentSortOption;

    sortDropdown.addEventListener("change", event => {
        const selectedSortOption = event.target.value;
        saveSortingPreference(selectedSortOption);
        currentSortOption = selectedSortOption;
        currentPage = 1;

        if (currentQuery) {
            fetchPostsBySearch(currentQuery, selectedSortOption, currentPage, false);
        } else {
            fetchPosts(currentPage, selectedSortOption, false);
        }

        updateUrlForPagination(selectedSortOption, currentPage, currentQuery, true);
    });
}

function saveSortingPreference(sortOption) {
    localStorage.setItem("sortingPreference", sortOption);
}

function getSortingPreference() {
    return localStorage.getItem("sortingPreference") || null;
}

// Edit Post Window
function openEditModal(post, postId) {
    const modal = document.createElement("div");
    modal.classList.add("modal");

    modal.innerHTML = `
        <div class="modal-overlay">
            <div class="modal-content form-container">
                <h2>Edit Post</h2>
                <form id="edit-form">
                    <input type="text" name="title" placeholder="Edit Post Title" value="${post.title}" required>
                    <div id="editor-edit" class="editor-container quill-editor"></div>
                    <input type="hidden" name="content" id="hidden-content-edit">
                    <div class="modal-actions">
                    <button id="saveBtn" type="submit">Save</button>
                    <button type="button" id="close-modal">Cancel</button>
                    </div>
                </form>
            </div>
        </div>
    `;

    // Append the modal to the body
    document.body.appendChild(modal);

    // Dynamically load Quill CSS
    const quillStyle = document.createElement("style");
    quillStyle.textContent = `
         .modal-overlay {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0, 0, 0, 0.5);
            display: flex;
            justify-content: center;
            align-items: center;
            z-index: 1000;
        }
        /* Modal content */
        .modal-content {
            padding: 20px;
            border-radius: 8px;
            width: 100%;
            max-width: 600px;
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
            position: relative;
            z-index: 1001;
        }
        .form-container {
            width: 90%;
            max-width: 600px;
            margin: 0 auto;
            padding: 20px;
            background:rgb(13, 33, 13);
            border-radius: 10px;
            box-shadow: 0px 4px 10px rgba(0, 0, 0, 0.1);
            text-align: center;
        }
        .form-container input[type="text"] {
            width: 100% ;
            padding: 10px;
            margin: 10px 0;
            border: 1px solid #ddd;
            border-radius: 5px;
            font-size: 16px;
            font-family: 'Nunito', sans-serif;
            resize: none;
        }
        .form-container h2{
            color: #f8f7c5;
        }
        .form-container button {
            width: 100%;
            padding: 10px;
            background: #4a7c59;
            color: #f8f7c5;
            font-size: 16px;
            border: none;
            border-radius: 30px;
            cursor: pointer;
            transition: 0.3s;
        }
        #saveBtn,#close-modal{
            margin-top:15px;
        }
        .form-container button:hover {
            background: #3a5c44;
        }
        .editor-container {
            height: 200px;
            background:#f8f7c5;
        }
        
        .ql-editor::-webkit-scrollbar {
            width: 6px;
        }
        
        .ql-editor::-webkit-scrollbar-track {
            background: #e1d5ae;
            border-radius: 5px;
        }
        
        .ql-editor::-webkit-scrollbar-thumb {
            background: rgba(0, 0, 0, 0.4);
            border-radius: 5px;
        }
                    
        input,
        #editor .ql-editor {
            background-color: #f8f7c5;
        }
        .ql-toolbar {
            background-color: #e1d5ae;
            text-align: left;
            border-top-left-radius: 5px;
            border-top-right-radius: 5px;
        }
        .ql-toolbar button:hover {
            background-color: transparent !important;
        }
    `;
    document.head.appendChild(quillStyle);

    // Initialize Quill editor
    const editorContainer = modal.querySelector("#editor-edit");
    const quill = new Quill(editorContainer, {
        theme: "snow",
        placeholder: "Edit your thoughts...",
        modules: {
            toolbar: [
                [{ header: [3, false] }],
                ["bold", "italic", "underline", "strike"],
                [{ color: [] }, { background: [] }],
                [{ align: [] }],
                [{ list: "ordered" }],
                ['link', 'code-block'],
                ["clean"]
            ]
        }
    });

    // Populate the editor with the post's content
    quill.root.innerHTML = post.content;

    function removeImages() {
        const images = quill.root.querySelectorAll('img');
        images.forEach(img => img.remove());
    }

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

    removeImages();
    quill.on('text-change', function () {
        removeImages();
    });

    // Handle form submission
    modal.querySelector("#edit-form").onsubmit = function (e) {
        e.preventDefault();
        const titleInput = this.querySelector('input[name="title"]');
        const title = titleInput.value.trim();
        if (!title) {
            openModal("Title cannot be empty!");
            return;
        }

        let plainText = quill.getText().trim().replace(/\n{2,}/g, "\n");
        let htmlContent = quill.root.innerHTML
            .trim()
            .replace(/(<p><br><\/p>)+/g, "")
            .replace(/<span class="ql-ui" contenteditable="false"><\/span>/g, "");

        if (!plainText) {
            openModal("Post cannot be empty!");
            return;
        }

        modal.querySelector("#hidden-content-edit").value = htmlContent;

        // Construct the URL-encoded payload
        const formData = new URLSearchParams();
        formData.append("title", this.title.value);
        formData.append("content", htmlContent);

        fetch(`/Eden/editPost/${postId}`, {
            method: "PUT",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: formData.toString()
        })
            .then((response) => {
                if (response.status === 204) {
                    fetchPosts(currentPage, currentSortOption);
                    modal.remove();
                    quillStyle.remove();
                } else if (response.status === 403) {
                    return response.json();
                } else if (response.status === 429) {
                    window.location.href = "code429.html";
                } else {
                    throw new Error("Failed to update post.");
                }
            }).then(data => {
                if (data && data.redirectUrl) {
                    window.location.href = data.redirectUrl;
                }
            })
            .catch((error) => {
                console.error("Error updating post:", error);
                openModal("An error occurred while updating the post.");
            });
    };

    // Close the modal
    modal.querySelector("#close-modal").onclick = () => {
        modal.remove();
        quillStyle.remove();
    };
}
// Edit Comment Window
function openEditCommentModal(commentId) {
    fetch(`/Eden/getCommentDetails/${commentId}`)
        .then(response => {
            if (response.status === 429) {
                window.location.href = "code429.html";
            } else if (!response.ok) {
                throw new Error("Failed to fetch comment details.");
            }
            return response.json();
        })
        .then(comment => {
            // Create the modal
            const modal = document.createElement("div");
            modal.classList.add("modal");

            modal.innerHTML = `
                <div class="modal-comment-overlay">
                    <div class="modal-comment-content form-comment-modal-container">
                        <h2>Edit Comment</h2>
                        <form id="edit-comment-form">
                            <textarea name="content" placeholder="Edit your comment..." required>${comment.content}</textarea>
                            <input type="hidden" name="commentId" value="${commentId}">
                            <div class="modal-actions">
                            <button id="save-comment-btn" type="submit">Save</button>
                            <button type="button" id="close-comment-modal">Cancel</button>
                            </div>
                        </form>
                    </div>
                </div>
            `;

            document.body.appendChild(modal);

            // Handle form submission
            modal.querySelector("#edit-comment-form").onsubmit = function (e) {
                e.preventDefault();
                const content = this.querySelector('textarea[name="content"]').value.trim();

                if (!content) {
                    openModal("Comment cannot be empty!");
                    return;
                }

                const formData = new URLSearchParams();
                formData.append("content", content);

                fetch(`/Eden/editComment/${commentId}`, {
                    method: "PUT",
                    headers: { "Content-Type": "application/x-www-form-urlencoded" },
                    body: formData.toString()
                })
                    .then(response => {
                        if (response.status === 204) {
                            const commentDiv = document.querySelector(`.comment[data-commentid="${commentId}"]`);
                            if (commentDiv) commentDiv.getElementsByClassName("comment-content-text")[0].textContent = content;
                            modal.remove();
                        } else if (response.status === 403) {
                            return response.json();
                        } else if (response.status === 429) {
                            window.location.href = "code429.html";
                        } else {
                            throw new Error("Failed to update comment.");
                        }
                    }).then(data => {
                        if (data && data.redirectUrl) {
                            window.location.href = data.redirectUrl;
                        }
                    })
                    .catch(error => {
                        console.error("Error updating comment:", error);
                        openModal("An error occurred while updating the comment.");
                    });
            };

            // Close the modal
            modal.querySelector("#close-comment-modal").onclick = () => {
                modal.remove();
            };
        })
        .catch(error => {
            console.error("Error fetching comment details:", error);
            openModal("An error occurred while fetching comment details.");
        });
}
// Delete Comment
function deleteComment(commentId) {
    openConfirmModal("Are you sure you want to delete this comment?")
        .then((isConfirmed) => {
            if (!isConfirmed) {
                return;
            }
            fetch(`/Eden/deleteComment/${commentId}`, { method: "DELETE" })
                .then(response => {
                    if (response.status === 204) {
                        const commentDiv = document.querySelector(`.comment[data-commentid="${commentId}"]`);
                        if (commentDiv) commentDiv.remove();
                    } else if (response.status === 403) {
                        return response.json();
                    } else if (response.status === 429) {
                        window.location.href = "code429.html";
                    } else {
                        throw new Error("Failed to delete comment.");
                    }
                })
                .then(data => {
                    if (data && data.redirectUrl) {
                        window.location.href = data.redirectUrl;
                    }
                })
                .catch(error => {
                    console.error("Error deleting comment:", error);
                    openModal("An error occurred while deleting the comment.");
                });
        });
}
