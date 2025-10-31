-- =====================================================================
-- V4__Insert_Default_Plans.sql
-- Inserta los planes de suscripción iniciales.
-- INCLUYE: Plan Trial interno + 3 Planes de Pago con IDs de PayPal.
-- =====================================================================

-- =============================================
-- PLAN 0: TRIAL (Gratuito - Gestión Interna)
-- Este plan NO existe en PayPal. Define los límites durante la prueba.
-- =============================================
INSERT INTO plans (
    code, name, tier, description,
    monthly_price, annual_price, currency,
    max_contacts, max_users, max_pipelines, max_deals, max_storage_gb,
    active, is_featured, sort_order, created_by
) VALUES (
    'TRIAL', 
    'Plan de Prueba', 
    'STARTER', -- El trial tiene los límites del tier más bajo
    'Acceso por 21 días para probar la plataforma.',
    0, 0, 'USD', -- Precio 0, moneda USD para consistencia
    100, 1, 1, 50, 1,
    true, false, 0, 'SYSTEM'
);

-- =============================================
-- PLAN 1: EKLESA STARTER (Plan de pago más bajo)
-- =============================================
INSERT INTO plans (
    code, name, tier, description,
    monthly_price, annual_price, currency,
    max_contacts, max_users, max_pipelines, max_deals, max_storage_gb,
    paypal_monthly_plan_id, paypal_annual_plan_id,
    active, is_featured, sort_order, created_by
) VALUES (
    'STARTER',
    'Eklesa Starter',
    'STARTER',
    'CRM completo. Límites: 1 pipeline, 1 usuario admin. Dirigido a: Iglesias pequeñas, emprendimientos y negocios pequeños.',
    25.00,        -- Precio mensual de tu pantallazo
    250.00,       -- Precio anual (25.00 * 10)
    'USD',        -- Moneda de tu pantallazo
    100, 1, 1, 50, 1, -- Límites basados en la descripción
    'P-4ET45816MX305514CNEBA45A', -- ✅ ID MENSUAL REAL de tu pantallazo
    'P-0VH38667S0493551LNEBBMXQ', -- ⚠️ ID de plan anual en PayPal
    true, false, 1, 'SYSTEM'
);

-- =============================================
-- PLAN 2: EKLESA PROFESSIONAL (Plan Intermedio)
-- =============================================
INSERT INTO plans (
    code, name, tier, description,
    monthly_price, annual_price, currency,
    max_contacts, max_users, max_pipelines, max_deals, max_storage_gb,
    paypal_monthly_plan_id, paypal_annual_plan_id,
    active, is_featured, sort_order, created_by
) VALUES (
    'PROFESSIONAL',
    'Eklesa Professional',
    'PROFESSIONAL',
    'CRM + Turns + Automatizaciones + Roles y permisos + Reportes. Usuarios ilimitados, hasta 5 pipelines.',
    59.00,        -- Precio mensual de tu pantallazo
    590.00,       -- Precio anual (59.00 * 10)
    'USD',        -- Moneda de tu pantallazo
    5000, NULL, 5, 1000, 50, -- Límites basados en la descripción (NULL = ilimitado)
    'P-1Y3088515N8604148NEBBRQ',   -- ✅ ID MENSUAL REAL de tu pantallazo
    'P-29G68253UV706715HNEBBPWQ', -- ⚠️ ID de plan anual en PayPal
    true, true, 2, 'SYSTEM' -- Marcado como "is_featured"
);

-- =============================================
-- PLAN 3: EKLESA ENTERPRISE (Plan más alto)
-- =============================================
INSERT INTO plans (
    code, name, tier, description,
    monthly_price, annual_price, currency,
    max_contacts, max_users, max_pipelines, max_deals, max_storage_gb,
    paypal_monthly_plan_id, paypal_annual_plan_id,
    active, is_featured, sort_order, created_by
) VALUES (
    'ENTERPRISE',
    'Eklesa Enterprise',
    'ENTERPRISE',
    'CRM + Turns + Soporte priorizado (SLA). Usuarios y pipelines ilimitados, dirigido a iglesias o empresas grandes.',
    129.00,       -- Precio mensual de tu pantallazo
    1290.00,      -- Precio anual (129.00 * 10)
    'USD',        -- Moneda de tu pantallazo
    NULL, NULL, NULL, NULL, NULL, -- Límites ilimitados (NULL)
    'P-9SK50950L0634912KNEBBGUY',   -- ✅ ID MENSUAL REAL de tu pantallazo
    'P-93W87582YJ441335DNEBBRWI', -- ⚠️ ID de plan anual en PayPal
    true, false, 3, 'SYSTEM'
);

-- =============================================
-- PARTE 5: FEATURES DE CADA PLAN (VERSIÓN COMPLETA)
-- =============================================

-- =============================================
-- Features del Plan STARTER
-- =============================================

INSERT INTO plan_features (plan_id, feature_code, feature_name, description, type, enabled, limit_value, sort_order, created_by)
SELECT id, 'CRM_COMPLETE', 'CRM Completo', 'Acceso a todas las funcionalidades del CRM.', 'MODULE', true, NULL, 1, 'SYSTEM' FROM plans WHERE code = 'STARTER'
UNION ALL
SELECT id, 'MAX_PIPELINES', '1 Pipeline', 'Límite de 1 pipeline activo.', 'LIMIT', true, '1', 2, 'SYSTEM' FROM plans WHERE code = 'STARTER'
UNION ALL
SELECT id, 'MAX_USERS', '1 Usuario Admin', 'Acceso para un único usuario administrador.', 'LIMIT', true, '1', 3, 'SYSTEM' FROM plans WHERE code = 'STARTER'
UNION ALL
SELECT id, 'SUPPORT_STANDARD', 'Soporte Estándar', 'Soporte a través de email y centro de ayuda.', 'SUPPORT', true, NULL, 4, 'SYSTEM' FROM plans WHERE code = 'STARTER';


-- =============================================
-- Features del Plan PROFESSIONAL
-- =============================================

INSERT INTO plan_features (plan_id, feature_code, feature_name, description, type, enabled, limit_value, sort_order, created_by)
SELECT id, 'CRM_COMPLETE', 'CRM Completo', 'Acceso a todas las funcionalidades del CRM.', 'MODULE', true, NULL, 1, 'SYSTEM' FROM plans WHERE code = 'PROFESSIONAL'
UNION ALL
SELECT id, 'TURNS_SERVICE', 'Módulo de Turnos', 'Acceso completo al servicio de gestión de turnos.', 'MODULE', true, NULL, 2, 'SYSTEM' FROM plans WHERE code = 'PROFESSIONAL'
UNION ALL
SELECT id, 'AUTOMATIONS', 'Automatizaciones', 'Creación de flujos de trabajo y reglas automáticas.', 'CAPABILITY', true, NULL, 3, 'SYSTEM' FROM plans WHERE code = 'PROFESSIONAL'
UNION ALL
SELECT id, 'ROLES_PERMISSIONS', 'Roles y Permisos', 'Gestión de roles de usuario y permisos granulares.', 'CAPABILITY', true, NULL, 4, 'SYSTEM' FROM plans WHERE code = 'PROFESSIONAL'
UNION ALL
SELECT id, 'REPORTS', 'Reportes Avanzados', 'Acceso al módulo completo de reportes y dashboards.', 'CAPABILITY', true, NULL, 5, 'SYSTEM' FROM plans WHERE code = 'PROFESSIONAL'
UNION ALL
SELECT id, 'UNLIMITED_USERS', 'Usuarios Ilimitados', 'Sin límite en el número de usuarios.', 'LIMIT', true, 'UNLIMITED', 6, 'SYSTEM' FROM plans WHERE code = 'PROFESSIONAL'
UNION ALL
SELECT id, 'MAX_PIPELINES', 'Hasta 5 Pipelines', 'Límite de 5 pipelines activos.', 'LIMIT', true, '5', 7, 'SYSTEM' FROM plans WHERE code = 'PROFESSIONAL'
UNION ALL
SELECT id, 'SUPPORT_PRIORITY', 'Soporte Prioritario', 'Atención prioritaria para tus solicitudes.', 'SUPPORT', true, NULL, 8, 'SYSTEM' FROM plans WHERE code = 'PROFESSIONAL';


-- =============================================
-- Features del Plan ENTERPRISE
-- =============================================

INSERT INTO plan_features (plan_id, feature_code, feature_name, description, type, enabled, limit_value, sort_order, created_by)
SELECT id, 'ALL_IN_PROFESSIONAL', 'Todo en Professional', 'Incluye todas las características del plan Professional.', 'CAPABILITY', true, NULL, 1, 'SYSTEM' FROM plans WHERE code = 'ENTERPRISE'
UNION ALL
SELECT id, 'UNLIMITED_PIPELINES', 'Pipelines Ilimitados', 'Sin límite en el número de pipelines.', 'LIMIT', true, 'UNLIMITED', 2, 'SYSTEM' FROM plans WHERE code = 'ENTERPRISE'
UNION ALL
SELECT id, 'DEDICATED_SUPPORT', 'Soporte Priorizado (SLA)', 'Soporte con acuerdos de nivel de servicio (SLA).', 'SUPPORT', true, NULL, 3, 'SYSTEM' FROM plans WHERE code = 'ENTERPRISE'
UNION ALL
SELECT id, 'API_ACCESS', 'Acceso a API', 'Capacidad de integrarse con otros sistemas vía API.', 'CAPABILITY', true, NULL, 4, 'SYSTEM' FROM plans WHERE code = 'ENTERPRISE';