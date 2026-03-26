import Page from "./app/Page"
import { ThemeToggle } from "./app/ThemeToggle"

export function App() {
    return (
        <div className="min-h-screen bg-background text-foreground">
            <ThemeToggle />
            <Page />
        </div>
    )
}
export default App
