'use client'

import { useState } from "react";
import { QrForm, type QrFormValues } from "./QrForm";
import { generateQr, useAuthGuard } from "@/lib/api";
import DataTable from "./DataTable";
import { logout } from "@/lib/utils";


export const handleLogout = async () => {
    await logout()
    window.location.replace("/login")
}

export default function Page() {
    const [imageUrl, setImageUrl] = useState<string | null>(null);
    const [refreshKey, setRefreshKey] = useState(0);

    const loading = useAuthGuard()
    if (loading) return null

    const handleSubmit = async (values: QrFormValues) => {
        const blob = await generateQr(values);

        if (imageUrl) URL.revokeObjectURL(imageUrl);

        const typedBlob = blob.type
            ? blob
            : new Blob([blob], { type: "image/svg+xml" });

        const url = URL.createObjectURL(typedBlob);
        setImageUrl(url);

        setRefreshKey(prev => prev + 1);
    };

    return (
        <div className="w-screen min-h-screen flex flex-col">

            {/* TOP: 2-column layout */}
            <div className="flex flex-1">

                {/* Left */}
                <div className="w-1/2 flex items-center justify-center">
                    <div className="w-full max-w-xl flex flex-col gap-6 px-8">
                        <QrForm onSubmit={handleSubmit} />
                    </div>
                </div>

                {/* Right */}
                <div className="w-1/2 flex items-center justify-center">
                    {imageUrl && (
                        <img
                            src={imageUrl}
                            className="max-w-[385px] w-full"
                        />
                    )}
                </div>
            </div>

            {/* BOTTOM: centered table */}
            <div className="w-full flex justify-center py-10">
                <div className="w-full max-w-4xl">
                    <DataTable refreshKey={refreshKey} onRefresh={() => setRefreshKey(k => k + 1)} />
                </div>
            </div>

        </div>
    );
}
