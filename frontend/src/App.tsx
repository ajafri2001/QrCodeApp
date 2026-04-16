import Login from "./app/Login";
import Page, { handleLogout } from "./app/Page";
import SignUp from "./app/SignUp";
import { AppHeader, ThemeToggle } from "./app/ThemeToggle";
import { Routes, Route } from "react-router-dom";

/**
 * Layout wrapper for authenticated/main QR application page.
 * Includes global header and main page content.
 */
function QrPageLayout() {
    return (
        <div className="min-h-screen bg-background text-foreground">
            {/* Top navigation bar with logout action */}
            <AppHeader onLogout={handleLogout} />

            {/* Main application content */}
            <Page />
        </div>
    );
}

/**
 * Root application router.
 * Defines public routes (login/signup) and main app entry.
 */
export function App() {
    return (
        <>
            {/* Global theme switcher (light/dark) */}
            <ThemeToggle />

            {/* Application routes */}
            <Routes>
                <Route path="/" element={<QrPageLayout />} />
                <Route path="/login" element={<Login />} />
                <Route path="/signup" element={<SignUp />} />
            </Routes>
        </>
    );
}

export default App;
