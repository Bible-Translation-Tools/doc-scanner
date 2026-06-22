import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        // Start Koin once at app launch.
        MainViewControllerKt.startKoinForIos()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
