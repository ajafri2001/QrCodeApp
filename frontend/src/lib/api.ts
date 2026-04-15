import type { QrFormValues } from "@/app/QrForm"
import { useEffect, useState } from "react"

export async function generateQr(values: QrFormValues) {
    const res = await fetch("/api/getQr", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify(values),
    })

    if (!res.ok) {
        const text = await res.text()
        console.error("API error:", res.status, text)
        throw new Error("Failed")
    } else {
        console.log("Nice we got the request")
    }


    return await res.blob()
}

export function useAuthGuard() {
    const [loading, setLoading] = useState(true)

    useEffect(() => {
        fetch("/api/me", { credentials: "include" })
            .then(res => {
                if (!res.ok) {
                    window.location.href = "/login"
                }
            })
            .finally(() => setLoading(false))
    }, [])

    return loading
}

export function useRedirectIfAuth() {
    const [loading, setLoading] = useState(true)

    useEffect(() => {
        fetch("/api/me", {
            credentials: "include",
        })
            .then(res => {
                if (res.ok) {
                    window.location.href = "/"
                }
            })
            .finally(() => setLoading(false))
    }, [])

    return loading
}
