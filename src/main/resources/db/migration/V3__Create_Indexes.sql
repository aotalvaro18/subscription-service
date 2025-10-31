-- V3__Create_Indexes.sql

-- Índices para plans
CREATE INDEX idx_plans_code ON plans(code);
CREATE INDEX idx_plans_tier ON plans(tier);
CREATE INDEX idx_plans_active ON plans(active);
CREATE INDEX idx_plans_sort_order ON plans(sort_order);

-- Índices para plan_features
CREATE INDEX idx_plan_features_plan_id ON plan_features(plan_id);
CREATE INDEX idx_plan_features_code ON plan_features(feature_code);

-- Índices para subscriptions
CREATE INDEX idx_subscriptions_organization_id ON subscriptions(organization_id);
CREATE INDEX idx_subscriptions_status ON subscriptions(status);
CREATE INDEX idx_subscriptions_paypal_id ON subscriptions(paypal_subscription_id);
CREATE INDEX idx_subscriptions_trial_end ON subscriptions(trial_end_date);

-- Índices para invoices
CREATE INDEX idx_invoices_subscription_id ON invoices(subscription_id);
CREATE INDEX idx_invoices_status ON invoices(status);
CREATE INDEX idx_invoices_created_at ON invoices(created_at DESC);

-- Índices para usage_records
CREATE INDEX idx_usage_records_subscription_id ON usage_records(subscription_id);
CREATE INDEX idx_usage_records_feature_code ON usage_records(feature_code);
CREATE INDEX idx_usage_records_recorded_at ON usage_records(recorded_at DESC);