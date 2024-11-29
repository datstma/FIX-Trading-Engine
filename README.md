# FIX Protocol Trading Engine Demo

## Overview
This project demonstrates a basic implementation of an automated trading engine using the FIX (Financial Information eXchange) protocol. It's designed as an educational resource to show how one can interact with FIX protocol streams in a hands-on way, particularly useful for those interested in building their own trading systems.

*Important Note: This code is meant for educational purposes and demonstration only. It is not production-ready and should not be used in live trading environments without significant improvements.*

## What This Project Shows

* Basic FIX protocol message handling
* Integration with Excel for trade management
* Real-time order processing
* Simple automated trading logic
* FIX message construction and parsing
* Database integration for trade tracking

## Core Components

### SendToFIX.java
Handles outgoing FIX messages and manages the connection to the FIX server. Shows basic message construction and session management.

### ProcessFromFIX.java
Processes incoming FIX messages, demonstrating how to parse and handle various FIX message types.

### OrderChecker.java
Monitors Excel files for new orders and translates them into FIX messages. Shows how to integrate external data sources with FIX trading.

### GlobalVars.java
Maintains session state and global configurations. Demonstrates basic state management in a FIX trading system.

## Database Structure

The project uses MySQL to store:
* Order status and history
* Trade execution details
* System logs
* Collateral information

## Excel Integration

* Trade orders are read from Excel files
* Format: Symbol, Quantity, Price, Side (Buy/Sell)
* System monitors for file changes
* Orders are converted to FIX messages

## Getting Started

### Prerequisites
* Java Development Kit (JDK)
* MySQL Database or any other relational database
* Microsoft Excel
* FIX Protocol counterparty (broker/exchange connection)

### Configuration
1. Database setup (tables for orders, executions, logs)
2. FIX session parameters (in configuration file)
3. Excel template setup
4. Logging configuration

## Educational Value

This project helps understand:
* FIX Protocol basics
* Message flow in trading systems
* Order lifecycle management
* Integration of multiple components
* Real-time data processing

## Limitations and Considerations

* Not optimized for high-frequency trading
* Basic error handling
* Limited risk management
* Simplified authentication
* No market data handling
* Basic order types only

## Common Use Cases

* Learning FIX protocol implementation
* Understanding trading system architecture
* Testing FIX connectivity
* Prototyping trading strategies
* Educational demonstrations

## Project Structure

### Source Files
* Communication Layer (FIX handling)
* Business Logic (order processing)
* Data Access (database operations)
* Utility Classes (helpers and tools)

### Configuration
* Database settings
* FIX session parameters
* Logging preferences
* Excel file locations

## Development Notes

* Code prioritizes readability over efficiency
* Comments explain FIX-specific concepts
* Simple implementations for learning purposes
* Demonstrates basic patterns in trading systems

## Contributing

Feel free to:
* Submit issues and suggestions
* Propose improvements
* Share learning experiences
* Suggest better practices

## Disclaimer

This code is:
* NOT production-ready
* NOT optimized for performance
* NOT secure for real trading
* FOR EDUCATIONAL PURPOSES ONLY
* A DEMONSTRATION of concepts

## Learning Resources

### FIX Protocol
* FIX Protocol Specification
* Common message types
* Session management
* Message construction

### Trading Concepts
* Order types
* Trade lifecycle
* Risk management
* Market connectivity

## Future Improvements

Potential areas for enhancement:
* Better error handling
* More order types
* Market data processing
* Risk checks
* Performance optimization

## Contact

Feel free to reach out for:
* Questions about FIX implementation
* Trading system architecture
* Learning resources
* Collaboration opportunities

Remember: This is a learning tool to understand FIX protocol and trading system basics. Use it to explore and understand, but build proper safeguards and optimizations for any production system.

## Database Setup

### Required Tables

* Create a MySQL database and run these SQL commands:

MariaDB Commands:

```sql
CREATE DATABASE trader;
USE trader;

CREATE TABLE collateral (
id INT AUTO_INCREMENT PRIMARY KEY,
CollRptID VARCHAR(255),
TotalNetValue DECIMAL(15,2),
MarginExcess DECIMAL(15,2),
timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE trader_order_status_log (
id INT AUTO_INCREMENT PRIMARY KEY,
trade_order_id VARCHAR(255),
status VARCHAR(255),
timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE trader_sequence_log (
id INT AUTO_INCREMENT PRIMARY KEY,
mysequence INT,
message_was TEXT,
timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE live_trades (
ID VARCHAR(255) PRIMARY KEY,
SYMBOL VARCHAR(50),
QUANTITY INT,
SIDE VARCHAR(10),
STATUS VARCHAR(50),
VARDE DECIMAL(15,2),
COMMISSION DECIMAL(15,2),
TIMESTAMP TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Building the Project

### Using Maven

1. Clone the repository:
```
git clone [repository-url]
cd trader
```
2. Build the project:
```   
mvn clean install
```
3. The build will create two JAR files in the target directory:
    * trader-1.0-SNAPSHOT.jar (without dependencies)
    * trader-1.0-SNAPSHOT-jar-with-dependencies.jar (complete runnable jar)

## Running the Application

### Configuration Files

1. Create a config.ini file in the project root:

[DATABASE]
url=jdbc:mysql://localhost:3306/trader
username=your_username
password=your_password

[FIX]
sendercompid=YOUR_SENDER_ID
targetcompid=YOUR_TARGET_ID
password=YOUR_FIX_PASSWORD
host=fix.example.com
port=9878

[EXCEL]
tradefile=C:/trading/orders.xlsx
interval=5000

2. Prepare Excel Trade File:
    * Create an Excel file as specified in your config
    * Required columns: Symbol, Quantity, Price, Side

### Running the Application

Method 1: Using JAR directly
```
java -jar target/trader-1.0-SNAPSHOT-jar-with-dependencies.jar
```
Method 2: Using Maven
```
mvn exec:java -Dexec.mainClass="trader.RunMe"
```
### Verifying Operation

1. Check Logs:
    * Application should create log files in the specified directory
    * Database tables should start populating with sequence logs

2. Monitor Database:
    * Use MySQL client to monitor tables:
```      
SELECT * FROM trader_sequence_log ORDER BY timestamp DESC LIMIT 10;
SELECT * FROM live_trades WHERE STATUS = 'New';
```
3. Excel Integration:
    * Add a test order in the Excel file
    * System should detect and process within configured interval

### Troubleshooting

Common Issues:

1. Database Connection:
    * Verify MySQL is running
    * Check credentials in config.ini
    * Ensure database and tables exist

2. FIX Connection:
    * Verify FIX server is accessible
    * Check firewall settings
    * Validate FIX credentials

3. Excel File:
    * Verify file path in config
    * Ensure file isn't open/locked
    * Check file permissions

### System Requirements

* Java 11 or higher
* MySQL 5.7 or higher
* Maven 3.6 or higher
* Microsoft Excel
* Minimum 4GB RAM
* Storage space for logs and database

### Environment Variables

Optional but recommended:
* JAVA_HOME: Pointing to JDK installation
* MAVEN_HOME: Pointing to Maven installation
* TRADER_CONFIG: Custom path to config.ini

### Development Setup

For IDE users:

IntelliJ IDEA:
1. File -> Open
2. Select the pom.xml
3. Enable Auto-Import for Maven
4. Run/Debug configuration for RunMe class

Eclipse:
1. File -> Import
2. Maven -> Existing Maven Projects
3. Select the project root
4. Create Run Configuration for RunMe class

### Logging Configuration

Default logging configuration creates:
* Application logs
* FIX message logs
* Error logs
* Database operation logs

/Stefan Månsby
