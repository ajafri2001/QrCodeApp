import { clsx, type ClassValue } from "clsx"
import { twMerge } from "tailwind-merge"

export function cn(...inputs: ClassValue[]) {
    return twMerge(clsx(inputs))
}

export async function logout() {
    await fetch("/api/logout", {
        method: "POST",
        credentials: "include",
    })
}

