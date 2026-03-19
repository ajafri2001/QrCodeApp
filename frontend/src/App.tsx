import { QrForm } from "./app/QrForm"
import { ThemeToggle } from "./app/ThemeToggle"

export function App() {
    return (
        <div className="flex min-h-screen items-center justify-center p-6 bg-background text-foreground">
            <ThemeToggle />
            <div className="w-full max-w-md flex flex-col gap-4">
                <QrForm />
            </div>
        </div>
    )
}

export default App
