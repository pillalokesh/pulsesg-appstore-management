const API_BASE = "/be/app/store/app-store";
const state = {
    allApps: [],
    visibleApps: [],
    view: "discover",
    category: "All applications",
    search: "",
    status: "all",
    appType: "all",
    sort: "name",
    loading: false,
    error: ""
};

const elements = {
    appGrid: document.querySelector("#app-grid"),
    categoryList: document.querySelector("#category-list"),
    catalogCount: document.querySelector("#catalog-count"),
    message: document.querySelector("#message"),
    searchInput: document.querySelector("#search-input"),
    sectionTitle: document.querySelector("#section-title"),
    viewLabel: document.querySelector("#view-label"),
    selectionSummary: document.querySelector("#selection-summary"),
    contextCard: document.querySelector("#context-card"),
    statusFilter: document.querySelector("#status-filter"),
    typeFilter: document.querySelector("#type-filter"),
    sortOrder: document.querySelector("#sort-order"),
    clearFilters: document.querySelector("#clear-filters"),
    detailsDialog: document.querySelector("#details-dialog"),
    detailsContent: document.querySelector("#details-content"),
    closeDialog: document.querySelector("#close-dialog"),
    tenantId: document.querySelector("#tenant-id"),
    storeType: document.querySelector("#store-type"),
    userId: document.querySelector("#user-id"),
    saveContext: document.querySelector("#save-context")
};

const contextStorageKey = "agent-store-context";

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

function initials(name) {
    const words = String(name || "App").trim().split(/\s+/).filter(Boolean);
    return words.slice(0, 2).map((word) => word[0]).join("").toUpperCase();
}

function loadContext() {
    try {
        const saved = JSON.parse(localStorage.getItem(contextStorageKey) || "{}");
        elements.tenantId.value = saved.tenantId || "";
        elements.storeType.value = saved.storeType || "";
        elements.userId.value = saved.userId || "";
    } catch (error) {
        localStorage.removeItem(contextStorageKey);
    }
}

function saveContext() {
    const context = {
        tenantId: elements.tenantId.value.trim(),
        storeType: elements.storeType.value.trim(),
        userId: elements.userId.value.trim()
    };
    localStorage.setItem(contextStorageKey, JSON.stringify(context));
    if (state.view === "my-apps" || state.view === "active-apps") {
        loadAssignedApps();
    }
}

function setMessage(text, isError = false) {
    elements.message.textContent = text;
    elements.message.classList.toggle("error", isError);
}

function appCategories() {
    return [...new Set(state.allApps.map((app) => app.category).filter(Boolean))].sort();
}

function renderCategories() {
    const categories = ["All applications", ...appCategories()];
    elements.categoryList.innerHTML = categories.map((category) => `
        <button type="button" class="category-button ${state.category === category ? "selected" : ""}" data-category="${escapeHtml(category)}">
            ${escapeHtml(category)}
        </button>
    `).join("");
}

function appTypes() {
    return [...new Set(state.allApps.map((app) => app.appType).filter(Boolean))].sort();
}

function renderTypeFilter() {
    const types = ["all", ...appTypes()];
    elements.typeFilter.innerHTML = types.map((type) => {
        const label = type === "all" ? "All types" : type;
        return `<option value="${escapeHtml(type)}">${escapeHtml(label)}</option>`;
    }).join("");
    elements.typeFilter.value = state.appType;
}

function filterApps() {
    const search = state.search.toLowerCase();
    state.visibleApps = state.allApps.filter((app) => {
        const categoryMatches = state.category === "All applications" || app.category === state.category;
        const statusMatches = state.status === "all" || String(app.status || "").toLowerCase() === state.status;
        const typeMatches = state.appType === "all" || app.appType === state.appType;
        const searchMatches = !search || [app.name, app.description, app.category, app.owner, app.appType]
            .filter(Boolean)
            .some((field) => String(field).toLowerCase().includes(search));
        return categoryMatches && statusMatches && typeMatches && searchMatches;
    });
    state.visibleApps.sort((first, second) => {
        if (state.sort === "recent") return String(second.createdOn || "").localeCompare(String(first.createdOn || ""));
        if (state.sort === "status") return String(first.status || "").localeCompare(String(second.status || ""));
        return String(first.name || "").localeCompare(String(second.name || ""));
    });
}

function appIcon(app) {
    if (app.icon) {
        return `<img src="${escapeHtml(app.icon)}" alt="" loading="lazy" onerror="this.parentElement.textContent='${escapeHtml(initials(app.name))}'">`;
    }
    return escapeHtml(initials(app.name));
}

function safeLaunchLink(link) {
    if (!link) return "";
    try {
        const url = new URL(link, window.location.origin);
        return ["http:", "https:"].includes(url.protocol) ? url.href : "";
    } catch (error) {
        return "";
    }
}

function renderCards() {
    filterApps();
    renderTypeFilter();
    elements.catalogCount.textContent = state.allApps.length;
    const hasFilters = state.search || state.category !== "All applications" || state.status !== "all" || state.appType !== "all";
    elements.selectionSummary.textContent = hasFilters
        ? `${state.visibleApps.length} application${state.visibleApps.length === 1 ? "" : "s"} match your filters`
        : "Showing the complete application catalog";

    if (state.loading) {
        elements.appGrid.innerHTML = `<div class="empty-state"><h3>Loading catalog</h3><p>Connecting to the organization application service.</p></div>`;
        return;
    }
    if (state.error) {
        elements.appGrid.innerHTML = `<div class="empty-state"><h3>Catalog unavailable</h3><p>${escapeHtml(state.error)}</p></div>`;
        return;
    }
    if (!state.visibleApps.length) {
        const title = state.search ? "No applications found" : "No applications available";
        elements.appGrid.innerHTML = `<div class="empty-state"><h3>${title}</h3><p>Try another category or search term.</p></div>`;
        return;
    }

    const accessLabel = state.view === "active-apps" ? "Active access" : state.view === "my-apps" ? "Assigned" : "Available to browse";
    elements.appGrid.innerHTML = state.visibleApps.map((app) => `
        <article class="app-card">
            <div class="app-card-top">
                <div class="app-icon">${appIcon(app)}</div>
                <div class="badge-stack">
                    <span class="status-badge ${String(app.status || "").toLowerCase() === "active" ? "status-active" : ""}">${escapeHtml(app.status || "Status unavailable")}</span>
                    <span class="access-badge">${accessLabel}</span>
                </div>
            </div>
            <h3>${escapeHtml(app.name || "Untitled application")}</h3>
            <p class="app-description">${escapeHtml(app.description || "No description available.")}</p>
            <div class="app-meta">
                ${app.category ? `<span>${escapeHtml(app.category)}</span>` : ""}
                ${app.version ? `<span>v${escapeHtml(app.version)}</span>` : ""}
                ${app.owner ? `<span>${escapeHtml(app.owner)}</span>` : ""}
                ${app.appType ? `<span>${escapeHtml(app.appType)}</span>` : ""}
            </div>
            <div class="card-actions">
                <button class="button button-outline details-button" type="button" data-app-id="${escapeHtml(app.id)}">Details</button>
                ${safeLaunchLink(app.link) ? `<a class="button button-link" href="${escapeHtml(safeLaunchLink(app.link))}" target="_blank" rel="noopener">Open</a>` : `<span class="button button-link disabled">No link</span>`}
            </div>
        </article>
    `).join("");
}

function renderView() {
    const labels = {
        discover: ["Discover", "Available applications"],
        "my-apps": ["My Apps", "Applications assigned to me"],
        "active-apps": ["Active Apps", "Currently active applications"],
        categories: ["Categories", "Browse by category"]
    };
    const [label, title] = labels[state.view] || labels.discover;
    elements.viewLabel.textContent = label;
    elements.sectionTitle.textContent = title;
    elements.statusFilter.value = state.status;
    elements.sortOrder.value = state.sort;
    document.querySelectorAll(".nav-link").forEach((link) => link.classList.toggle("active", link.dataset.view === state.view));
    elements.contextCard.hidden = state.view !== "my-apps" && state.view !== "active-apps";
    if (state.view === "categories") {
        state.category = state.category === "All applications" ? appCategories()[0] || "All applications" : state.category;
    }
    renderCategories();
    renderCards();
}

async function fetchJson(url, options = {}) {
    const response = await fetch(url, options);
    if (!response.ok) throw new Error(`The application service returned ${response.status}.`);
    return response.json();
}

async function loadCatalog() {
    state.loading = true;
    state.error = "";
    renderCards();
    try {
        state.allApps = await fetchJson(`${API_BASE}/apps`);
    } catch (error) {
        state.allApps = [];
        state.error = "Unable to load applications right now.";
    } finally {
        state.loading = false;
        renderView();
    }
}

async function loadAssignedApps() {
    const tenantId = elements.tenantId.value.trim();
    const storeType = elements.storeType.value.trim();
    if (!tenantId || !storeType) {
        state.allApps = [];
        state.error = "Enter a Tenant ID and Store type to view assigned applications.";
        renderView();
        return;
    }
    state.loading = true;
    state.error = "";
    renderCards();
    const endpoint = state.view === "active-apps" ? "user-active-apps" : "user-all-apps";
    const query = new URLSearchParams({ storeType });
    if (elements.userId.value.trim()) query.set("userId", elements.userId.value.trim());
    try {
        state.allApps = await fetchJson(`${API_BASE}/${endpoint}?${query}`, {
            headers: {
                "X-PULSE-TENANT-ID": tenantId,
                ...(elements.userId.value.trim() ? { "X-PULSE-SYS-GEN-USER-ID": elements.userId.value.trim() } : {})
            }
        });
    } catch (error) {
        state.allApps = [];
        state.error = "Unable to load assigned applications. Check the workspace context.";
    } finally {
        state.loading = false;
        renderView();
    }
}

function openDetails(appId) {
    const app = state.allApps.find((item) => String(item.id) === String(appId));
    if (!app) return;
    let metadata = "";
    if (app.externalAttributes) {
        try { metadata = JSON.stringify(JSON.parse(app.externalAttributes), null, 2); }
        catch (error) { metadata = app.externalAttributes; }
    }
    elements.detailsContent.innerHTML = `
        <div class="details-head">
            <div class="app-icon">${appIcon(app)}</div>
            <div><h2 id="details-title">${escapeHtml(app.name || "Untitled application")}</h2><p>${escapeHtml(app.category || "Application")}</p></div>
        </div>
        <div class="details-body">
            <h3>About this application</h3>
            <p>${escapeHtml(app.description || "No description available.")}</p>
            <div class="detail-grid">
                <div><strong>Status</strong><span>${escapeHtml(app.status || "Not specified")}</span></div>
                <div><strong>Version</strong><span>${escapeHtml(app.version || "Not specified")}</span></div>
                <div><strong>Owner</strong><span>${escapeHtml(app.owner || "Not specified")}</span></div>
                <div><strong>Application type</strong><span>${escapeHtml(app.appType || "Not specified")}</span></div>
                <div><strong>Access</strong><span>${escapeHtml(state.view === "active-apps" ? "Active access" : state.view === "my-apps" ? "Assigned" : "Available to browse")}</span></div>
                <div><strong>Created</strong><span>${escapeHtml(app.createdOn ? new Date(app.createdOn).toLocaleDateString() : "Not specified")}</span></div>
            </div>
            ${metadata ? `<h3>Metadata</h3><p>${escapeHtml(metadata)}</p>` : ""}
            ${safeLaunchLink(app.link) ? `<a class="button button-link" href="${escapeHtml(safeLaunchLink(app.link))}" target="_blank" rel="noopener">Open application</a>` : `<span class="button button-link disabled">No launch link available</span>`}
        </div>
    `;
    elements.detailsDialog.hidden = false;
}

function handleRoute() {
    const requestedView = window.location.hash.replace("#", "") || "discover";
    state.view = ["discover", "my-apps", "active-apps", "categories"].includes(requestedView) ? requestedView : "discover";
    state.search = "";
    state.status = "all";
    state.appType = "all";
    state.sort = "name";
    elements.searchInput.value = "";
    if (state.view === "discover" || state.view === "categories") loadCatalog();
    else loadAssignedApps();
}

elements.searchInput.addEventListener("input", (event) => {
    state.search = event.target.value;
    renderCards();
});
elements.statusFilter.addEventListener("change", (event) => {
    state.status = event.target.value;
    renderCards();
});
elements.typeFilter.addEventListener("change", (event) => {
    state.appType = event.target.value;
    renderCards();
});
elements.sortOrder.addEventListener("change", (event) => {
    state.sort = event.target.value;
    renderCards();
});
elements.clearFilters.addEventListener("click", () => {
    state.category = "All applications";
    state.search = "";
    state.status = "all";
    state.appType = "all";
    state.sort = "name";
    elements.searchInput.value = "";
    renderView();
});
elements.categoryList.addEventListener("click", (event) => {
    const button = event.target.closest("[data-category]");
    if (!button) return;
    state.category = button.dataset.category;
    renderCategories();
    renderCards();
});
elements.appGrid.addEventListener("click", (event) => {
    const button = event.target.closest(".details-button");
    if (button) openDetails(button.dataset.appId);
});
elements.closeDialog.addEventListener("click", () => { elements.detailsDialog.hidden = true; });
elements.detailsDialog.addEventListener("click", (event) => { if (event.target === elements.detailsDialog) elements.detailsDialog.hidden = true; });
elements.saveContext.addEventListener("click", saveContext);
window.addEventListener("hashchange", handleRoute);

loadContext();
handleRoute();
