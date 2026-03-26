import type { QrFormValues } from "@/app/QrForm"

export async function generateQr(values: QrFormValues) {
    const res = await fetch("/api/qr", {
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
