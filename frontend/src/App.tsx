import Login from "./app/Login";
import Page, { handleLogout } from "./app/Page"
import SignUp from "./app/SignUp";
import { AppHeader, ThemeToggle } from "./app/ThemeToggle"
import { Routes, Route } from "react-router-dom"

function QrPageLayout() {
    return (
        <div className="min-h-screen bg-background text-foreground">
            <AppHeader onLogout={handleLogout} />
            <Page />
        </div>
    );
}

export function App() {
    return (
        <>
            <ThemeToggle />
            <Routes>
                <Route path="/" element={<QrPageLayout />} />
                <Route path="/login" element={<Login />} />
                <Route path="/signup" element={<SignUp />} />
            </Routes>
        </>
    )
}

export default App
