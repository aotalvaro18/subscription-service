-- V2__Create_Subscription_Tables.sql

-- Tabla: plans
CREATE TABLE plans (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    tier VARCHAR(20) NOT NULL,
    description TEXT,
    monthly_price DECIMAL(10,2),
    annual_price DECIMAL(10,2),
    currency VARCHAR(3) NOT NULL DEFAULT 'COP',
    max_contacts INTEGER,
    max_users INTEGER,
    max_pipelines INTEGER,
    max_deals INTEGER,
    max_storage_gb INTEGER,
    paypal_monthly_plan_id VARCHAR(100),
    paypal_annual_plan_id VARCHAR(100),
    active BOOLEAN NOT NULL DEFAULT true,
    is_featured BOOLEAN DEFAULT false,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_tier CHECK (tier IN ('STARTER', 'PROFESSIONAL', 'ENTERPRISE'))
);
CREATE TRIGGER update_plans_updated_at BEFORE UPDATE ON plans FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Tabla: plan_features
CREATE TABLE plan_features (
    id BIGSERIAL PRIMARY KEY,
    plan_id BIGINT NOT NULL REFERENCES plans(id) ON DELETE CASCADE,
    feature_code VARCHAR(100) NOT NULL,
    feature_name VARCHAR(200) NOT NULL,
    description TEXT,
    type VARCHAR(30) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT true,
    limit_value VARCHAR(50),
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_feature_type CHECK (type IN ('MODULE', 'CAPABILITY', 'LIMIT', 'SUPPORT'))
);
CREATE TRIGGER update_plan_features_updated_at BEFORE UPDATE ON plan_features FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Tabla: subscriptions
CREATE TABLE subscriptions (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT UNIQUE NOT NULL,
    plan_id BIGINT NOT NULL REFERENCES plans(id),
    status VARCHAR(30) NOT NULL,
    billing_period VARCHAR(20),
    trial_start_date TIMESTAMP WITH TIME ZONE,
    trial_end_date TIMESTAMP WITH TIME ZONE,
    is_trial_used BOOLEAN NOT NULL DEFAULT false,
    paypal_payer_id VARCHAR(100),
    paypal_subscription_id VARCHAR(100) UNIQUE,
    paypal_agreement_id VARCHAR(100),
    paypal_email VARCHAR(255),
    current_period_start TIMESTAMP WITH TIME ZONE,
    current_period_end TIMESTAMP WITH TIME ZONE,
    next_billing_date TIMESTAMP WITH TIME ZONE,
    canceled_at TIMESTAMP WITH TIME ZONE,
    ended_at TIMESTAMP WITH TIME ZONE,
    amount DECIMAL(10,2),
    currency VARCHAR(3) DEFAULT 'COP',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_status CHECK (status IN ('TRIALING', 'ACTIVE', 'GRACE_PERIOD', 'PAST_DUE', 'SUSPENDED', 'CANCELED', 'ENDED')),
    CONSTRAINT chk_billing_period CHECK (billing_period IN ('MONTHLY', 'ANNUAL'))
);
CREATE TRIGGER update_subscriptions_updated_at BEFORE UPDATE ON subscriptions FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Tabla: invoices
CREATE TABLE invoices (
    id BIGSERIAL PRIMARY KEY,
    subscription_id BIGINT NOT NULL REFERENCES subscriptions(id) ON DELETE CASCADE,
    paypal_invoice_id VARCHAR(100),
    paypal_transaction_id VARCHAR(100),
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    period_start TIMESTAMP WITH TIME ZONE,
    period_end TIMESTAMP WITH TIME ZONE,
    paid_at TIMESTAMP WITH TIME ZONE,
    due_date TIMESTAMP WITH TIME ZONE,
    receipt_url VARCHAR(500),
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_invoice_status CHECK (status IN ('DRAFT', 'PENDING', 'PAID', 'FAILED', 'REFUNDED', 'VOID'))
);
CREATE TRIGGER update_invoices_updated_at BEFORE UPDATE ON invoices FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Tabla: usage_records
CREATE TABLE usage_records (
    id BIGSERIAL PRIMARY KEY,
    subscription_id BIGINT NOT NULL REFERENCES subscriptions(id) ON DELETE CASCADE,
    feature_code VARCHAR(50) NOT NULL,
    usage_count INTEGER NOT NULL DEFAULT 0,
    plan_limit INTEGER,
    usage_percentage DECIMAL(5,2),
    recorded_at TIMESTAMP WITH TIME ZONE NOT NULL,
    limit_exceeded BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0
);
CREATE TRIGGER update_usage_records_updated_at BEFORE UPDATE ON usage_records FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();