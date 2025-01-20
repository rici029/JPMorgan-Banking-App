# J. POO Morgan Chase & Co.

A Java-based banking system that implements various design patterns to manage transactions, accounts, and user operations.

## Design Patterns Used

### 1. Observer Pattern
- Implemented in the `Transactions` class
- Observers are notified when new transactions are added
- Allows for real-time transaction monitoring and updates
- Uses `TransactionObserver` interface for implementing observers

### 2. Command Pattern
- Commands are used to encapsulate all banking operations
- Each operation (e.g., AddAccount, CreateCard, PayOnline) is a separate command
- Provides easy extensibility for new banking operations
- Main commands are located in the `command/concrete` package

### 3. Factory Pattern
- `CommandFactory` creates appropriate command instances
- Centralizes command object creation
- Provides flexibility in adding new command types
- Maintains loose coupling between command creation and execution

### 4. Singleton Pattern
- `AppOperationsSingleton` manages global application state
- Ensures single point of access to application operations
- Thread-safe implementation with double-checked locking
- Manages user data, accounts, and transaction history

## Key Features

- Account management (creation, deletion, funds management)
- Card operations (create, delete, status checking)
- Online payments and money transfers
- Split payment functionality
- Transaction reporting and monitoring
- Business associate management
- Multiple currency support with exchange rates