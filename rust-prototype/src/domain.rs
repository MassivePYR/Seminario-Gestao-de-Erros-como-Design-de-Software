use std::fmt;

#[derive(Debug, PartialEq)]
pub enum TransferError {
    InsufficientFunds { available: f64, requested: f64 },
    AccountNotFound(String),
    NegativeAmount(f64),
    NetworkTimeout { retries: u32 },
    InvalidLedgerState(String),
}

impl fmt::Display for TransferError {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            TransferError::InsufficientFunds { available, requested } => {
                write!(f, "Saldo insuficiente: disponível R${:.2}, solicitado R${:.2}", available, requested)
            }
            TransferError::AccountNotFound(id) => {
                write!(f, "Conta não encontrada: {}", id)
            }
            TransferError::NegativeAmount(amount) => {
                write!(f, "Valor inválido: R${:.2} (deve ser positivo)", amount)
            }
            TransferError::NetworkTimeout { retries } => {
                write!(f, "Timeout de rede após {} tentativas", retries)
            }
            TransferError::InvalidLedgerState(msg) => {
                write!(f, "Estado inválido do ledger: {}", msg)
            }
        }
    }
}

// Erro irrecuperável: panic! — viola invariante do sistema
fn assert_ledger_integrity(balance: f64, account_id: &str) {
    if balance < 0.0 {
        panic!("VIOLAÇÃO DE INVARIANTE: conta '{}' com saldo negativo ({:.2}). Estado corrompido.", account_id, balance);
    }
}

#[derive(Debug, Clone)]
pub struct Account {
    pub id: String,
    pub owner: String,
    pub balance: f64,
}

impl Account {
    pub fn new(id: &str, owner: &str, initial_balance: f64) -> Self {
        assert!(initial_balance >= 0.0, "Saldo inicial não pode ser negativo");
        Account {
            id: id.to_string(),
            owner: owner.to_string(),
            balance: initial_balance,
        }
    }
}

pub struct Bank {
    accounts: Vec<Account>,
    pub transaction_log: Vec<String>,
}

impl Bank {
    pub fn new() -> Self {
        Bank {
            accounts: Vec::new(),
            transaction_log: Vec::new(),
        }
    }

    pub fn add_account(&mut self, account: Account) {
        self.accounts.push(account);
    }

    fn find_account(&self, id: &str) -> Option<usize> {
        self.accounts.iter().position(|a| a.id == id)
    }

    // Retorna Result<T, E> — o compilador obriga tratamento via #[must_use]
    #[must_use]
    pub fn transfer(&mut self, from_id: &str, to_id: &str, amount: f64) -> Result<f64, TransferError> {
        if amount <= 0.0 {
            return Err(TransferError::NegativeAmount(amount));
        }

        let from_idx = self.find_account(from_id)
            .ok_or_else(|| TransferError::AccountNotFound(from_id.to_string()))?;

        let to_idx = self.find_account(to_id)
            .ok_or_else(|| TransferError::AccountNotFound(to_id.to_string()))?;

        let available = self.accounts[from_idx].balance;
        if amount > available {
            return Err(TransferError::InsufficientFunds { available, requested: amount });
        }

        self.accounts[from_idx].balance -= amount;
        self.accounts[to_idx].balance += amount;

        // Garante integridade — panic! se invariante for violada
        assert_ledger_integrity(self.accounts[from_idx].balance, from_id);
        assert_ledger_integrity(self.accounts[to_idx].balance, to_id);

        let log_entry = format!(
            "[OK] {} → {}: R${:.2} (saldo restante: R${:.2})",
            from_id, to_id, amount, self.accounts[from_idx].balance
        );
        self.transaction_log.push(log_entry);

        Ok(self.accounts[from_idx].balance)
    }

    // Simula falha de rede com retry — erro recuperável após N tentativas
    pub fn transfer_with_retry(
        &mut self,
        from_id: &str,
        to_id: &str,
        amount: f64,
        max_retries: u32,
        simulate_failures: u32,
    ) -> Result<f64, TransferError> {
        for attempt in 0..max_retries {
            if attempt < simulate_failures {
                println!("  [tentativa {}/{}] Simulando timeout de rede...", attempt + 1, max_retries);
                std::thread::sleep(std::time::Duration::from_millis(100));
                continue;
            }
            return self.transfer(from_id, to_id, amount);
        }
        Err(TransferError::NetworkTimeout { retries: max_retries })
    }

    pub fn balance_of(&self, id: &str) -> Option<f64> {
        self.find_account(id).map(|idx| self.accounts[idx].balance)
    }
}
