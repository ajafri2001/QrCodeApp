'use client'

import { useState } from "react";
import { QrForm, type QrFormValues } from "./QrForm";
import { generateQr, useAuthGuard } from "@/lib/api";
import DataTable from "./DataTable";
import { logout } from "@/lib/utils";

// Global logout handler used by header/navigation
export const handleLogout = async () => {
    await logout()
    window.location.replace("/login")
}

export default function Page() {
    const [imageUrl, setImageUrl] = useState<string | null>(null);
    const [refreshKey, setRefreshKey] = useState(0);

    // Auth gate: prevents rendering before session validation completes
    const loading = useAuthGuard()
    if (loading) return null

    // Handle QR generation form submission
    const handleSubmit = async (values: QrFormValues) => {
        const blob = await generateQr(values);

        // Cleanup previous object URL to avoid memory leaks
        if (imageUrl) URL.revokeObjectURL(imageUrl);

        // Normalize blob type (fallback for missing content-type)
        const typedBlob = blob.type
            ? blob
            : new Blob([blob], { type: "image/svg+xml" });

        const url = URL.createObjectURL(typedBlob);
        setImageUrl(url);

        // Trigger table refresh after new QR generation
        setRefreshKey(prev => prev + 1);
    };

    return (
        <div className="w-screen min-h-screen flex flex-col">

            {/* TOP: split layout (form | preview) */}
            <div className="flex flex-1">

                {/* Left panel: QR form */}
                <div className="w-1/2 flex items-center justify-center">
                    <div className="w-full max-w-xl flex flex-col gap-6 px-8">
                        <QrForm onSubmit={handleSubmit} />
                    </div>
                </div>

                {/* Right panel: QR preview */}
                <div className="w-1/2 flex items-center justify-center">
                    {imageUrl && (
                        <img
                            src={imageUrl}
                            className="max-w-[385px] w-full"
                        />
                    )}
                </div>
            </div>

            {/* BOTTOM: history table */}
            <div className="w-full flex justify-center py-10">
                <div className="w-full max-w-4xl">
                    <DataTable
                        refreshKey={refreshKey}
                        onRefresh={() => setRefreshKey(k => k + 1)}
                    />
                </div>
            </div>

        </div>
    );
}
