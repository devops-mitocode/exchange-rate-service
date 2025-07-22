INSERT INTO exchange_rates (currency_pair, rate) VALUES
    ('USD_PEN', 3.60),
    ('EUR_PEN', 4.10),
    ('CNY_PEN', 0.50)
ON CONFLICT (currency_pair) DO NOTHING;