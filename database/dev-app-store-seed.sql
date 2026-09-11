-- Manual DEV seed data for the existing app_store and user_assigned_apps tables.
-- Run only after confirming the target database and schema. This script is not
-- executed automatically by Spring Boot or the deployment pipeline.

INSERT INTO app_store (
    name,
    description,
    category,
    status,
    owner,
    created_by,
    created_on,
    version,
    app_type,
    link
)
SELECT seed.name,
       seed.description,
       seed.category,
       seed.status,
       seed.owner,
       'platform-catalog',
       CURRENT_TIMESTAMP,
       seed.version,
       seed.app_type,
       NULL
FROM (VALUES
    ('Document Management', 'Organize, find, and securely share the documents your team depends on.', 'Productivity', 'Active', 'Workplace Platforms', '4.2.0', 'Internal'),
    ('Knowledge Hub', 'A searchable home for policies, playbooks, and institutional knowledge.', 'Productivity', 'Active', 'Enterprise Enablement', '3.8.1', 'Internal'),
    ('Team Workspace', 'Coordinate projects, discussions, and shared work across business teams.', 'Collaboration', 'Active', 'Digital Workplace', '5.1.0', 'Internal'),
    ('Meeting Manager', 'Plan meetings, capture decisions, and keep follow-up actions visible.', 'Collaboration', 'Active', 'Digital Workplace', '2.6.0', 'Internal'),
    ('Expense Manager', 'Submit, review, and track employee expenses through a consistent workflow.', 'Finance', 'Active', 'Finance Operations', '6.0.2', 'Internal'),
    ('Invoice Portal', 'Manage supplier invoices and keep payment operations moving efficiently.', 'Finance', 'Active', 'Accounts Payable', '4.7.0', 'Internal'),
    ('Employee Portal', 'Access employee services, company information, and everyday workplace tasks.', 'Human Resources', 'Active', 'People Operations', '7.3.0', 'Internal'),
    ('Business Analytics', 'Explore trusted business measures and performance insights in one place.', 'Analytics', 'Active', 'Data & Analytics', '3.4.2', 'Internal'),
    ('API Management', 'Publish, secure, and observe APIs used across the organization.', 'Developer Tools', 'Active', 'Platform Engineering', '8.1.0', 'Internal'),
    ('Developer Portal', 'Find service documentation, ownership details, and engineering standards.', 'Developer Tools', 'Active', 'Developer Experience', '2.9.4', 'Internal'),
    ('Access Management', 'Request and review access to organization systems and protected resources.', 'Security', 'Active', 'Identity & Security', '5.5.0', 'Internal'),
    ('Reporting Center', 'Create and access operational reports for recurring business decisions.', 'Analytics', 'Planned', 'Business Operations', '1.0.0', 'Internal')
) AS seed(name, description, category, status, owner, version, app_type)
WHERE NOT EXISTS (
    SELECT 1
    FROM app_store existing
    WHERE existing.name = seed.name
);

INSERT INTO user_assigned_apps (
    app_store_id,
    user_id,
    tenant_id,
    active,
    created_by,
    created_on,
    updated_by,
    updated_on,
    sort_order,
    roles
)
SELECT catalog.id,
       'catalog-user',
       'public',
       catalog.status = 'Active',
       'platform-catalog',
       CURRENT_TIMESTAMP,
       'platform-catalog',
       CURRENT_TIMESTAMP,
       catalog.sort_order,
       NULL
FROM (
    SELECT id,
           status,
           ROW_NUMBER() OVER (ORDER BY name) AS sort_order
    FROM app_store
    WHERE name IN (
        'Document Management',
        'Knowledge Hub',
        'Team Workspace',
        'Meeting Manager',
        'Expense Manager',
        'Invoice Portal',
        'Employee Portal',
        'Business Analytics',
        'API Management',
        'Developer Portal',
        'Access Management',
        'Reporting Center'
    )
) AS catalog
WHERE NOT EXISTS (
    SELECT 1
    FROM user_assigned_apps existing
    WHERE existing.app_store_id = catalog.id
      AND existing.user_id = 'catalog-user'
      AND existing.tenant_id = 'public'
);
