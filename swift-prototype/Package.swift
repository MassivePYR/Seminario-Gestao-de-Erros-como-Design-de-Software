// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "BankingSwift",
    targets: [
        .target(
            name: "BankingCore",
            path: "Sources/BankingCore"
        ),
        .executableTarget(
            name: "BankingApp",
            dependencies: ["BankingCore"],
            path: "Sources/BankingApp"
        ),
        .testTarget(
            name: "BankingTests",
            dependencies: ["BankingCore"],
            path: "Tests/BankingTests"
        ),
    ]
)
