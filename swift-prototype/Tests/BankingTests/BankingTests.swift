import XCTest
@testable import BankingCore

final class BankingTests: XCTestCase {

    var bank: Bank!

    override func setUp() {
        bank = Bank()
        bank.addAccount(Account(id: "A1", owner: "Alice", initialBalance: 1000.0))
        bank.addAccount(Account(id: "A2", owner: "Bob",   initialBalance: 500.0))
    }

    func testSuccessfulTransfer() throws {
        let remaining = try bank.transfer(from: "A1", to: "A2", amount: 200.0)
        XCTAssertEqual(remaining, 800.0, accuracy: 0.001)
        XCTAssertEqual(bank.balance(of: "A2") ?? 0, 700.0, accuracy: 0.001)
    }

    func testInsufficientFunds() {
        XCTAssertThrowsError(try bank.transfer(from: "A2", to: "A1", amount: 9000.0)) { error in
            guard case TransferError.insufficientFunds(let available, _) = error else {
                return XCTFail("Tipo de erro incorreto")
            }
            XCTAssertEqual(available, 500.0, accuracy: 0.001)
        }
    }

    func testAccountNotFound() {
        XCTAssertThrowsError(try bank.transfer(from: "X999", to: "A1", amount: 10.0)) { error in
            guard case TransferError.accountNotFound(let id) = error else {
                return XCTFail("Tipo de erro incorreto")
            }
            XCTAssertEqual(id, "X999")
        }
    }

    func testNegativeAmountRejected() {
        XCTAssertThrowsError(try bank.transfer(from: "A1", to: "A2", amount: -100.0)) { error in
            guard case TransferError.negativeAmount = error else {
                return XCTFail("Tipo de erro incorreto")
            }
        }
    }

    func testLedgerConservation() throws {
        let initial = bank.balance(of: "A1")! + bank.balance(of: "A2")!
        _ = try bank.transfer(from: "A1", to: "A2", amount: 300.0)
        let final_ = bank.balance(of: "A1")! + bank.balance(of: "A2")!
        XCTAssertEqual(initial, final_, accuracy: 0.001, "Conservação de valor violada")
    }

    func testNetworkRetrySuccess() throws {
        let remaining = try bank.transfer(from: "A1", to: "A2", amount: 100.0, maxRetries: 5, simulateFailures: 2)
        XCTAssertEqual(remaining, 900.0, accuracy: 0.001)
    }

    func testNetworkRetryExhausted() {
        XCTAssertThrowsError(
            try bank.transfer(from: "A1", to: "A2", amount: 100.0, maxRetries: 2, simulateFailures: 5)
        ) { error in
            guard case TransferError.networkTimeout(let retries) = error else {
                return XCTFail("Tipo de erro incorreto")
            }
            XCTAssertEqual(retries, 2)
        }
    }
}
