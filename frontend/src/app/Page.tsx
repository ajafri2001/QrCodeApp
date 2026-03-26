import { useState } from "react";
import { QrForm, type QrFormValues } from "./QrForm";
import { generateQr } from "@/lib/api";

export default function Page() {
    const [imageUrl, setImageUrl] = useState<string | null>(null);

    const handleSubmit = async (values: QrFormValues) => {
        const blob = await generateQr(values);
        if (imageUrl) URL.revokeObjectURL(imageUrl);
        const typedBlob = blob.type ? blob : new Blob([blob], { type: "image/svg+xml" });
        const url = URL.createObjectURL(typedBlob);
        setImageUrl(url);
    };

    return (
        <div className="w-screen h-screen flex">
            {/* Left: form centered in its half */}
            <div className="w-1/2 flex items-center justify-center">
                <div className="w-full max-w-xl flex flex-col gap-6 px-8">
                    <QrForm onSubmit={handleSubmit} />
                </div>
            </div>
            {/* Right: image centered in its half */}
            <div className="w-1/2 flex items-center justify-center">
                {imageUrl && (
                    <img
                        src={imageUrl}
                        className="max-w-[385px] w-full"
                    />
                )}
            </div>
        </div>
    );
}
