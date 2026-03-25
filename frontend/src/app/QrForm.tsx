"use client"

import { useState } from "react"
import {
    Field,
    FieldDescription,
    FieldGroup,
    FieldLabel,
    FieldLegend,
    FieldSet,
} from "@/components/ui/field"
import { Input } from "@/components/ui/input"

// ── types ────────────────────────────────────────────────────────────────────

type Ecc = "Low" | "Medium" | "Quartile" | "High"
type OutputFormat = "PNG" | "SVG"

export interface QrFormValues {
    url: string
    ecc: Ecc
    format: OutputFormat
    scale: number
    border: number
    darkColor: string
    lightColor: string
}

const DEFAULT: QrFormValues = {
    url: "",
    ecc: "Medium",
    format: "PNG",
    scale: 4,
    border: 4,
    darkColor: "#000000",
    lightColor: "#ffffff",
}

// ── sub-components ────────────────────────────────────────────────────────────

function SegmentedControl<T extends string>({
    options,
    value,
    onChange,
    label,
}: {
    options: { value: T; label: string; hint?: string }[]
    value: T
    onChange: (v: T) => void
    label: string
}) {
    return (
        <div
            role="radiogroup"
            aria-label={label}
            className="grid gap-1.5"
            style={{ gridTemplateColumns: `repeat(${options.length}, 1fr)` }}
        >
            {options.map((opt) => (
                <button
                    key={opt.value}
                    type="button"
                    role="radio"
                    aria-checked={value === opt.value}
                    onClick={() => onChange(opt.value)}
                    title={opt.hint}
                    className={[
                        "relative flex flex-col items-center justify-center gap-0.5 rounded-lg border px-2 py-2.5 text-xs font-medium transition-all duration-150 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring",
                        value === opt.value
                            ? "border-zinc-900 bg-zinc-900 text-white shadow-sm dark:border-zinc-100 dark:bg-zinc-100 dark:text-zinc-900"
                            : "border-zinc-200 bg-white text-zinc-500 hover:border-zinc-300 hover:text-zinc-700 dark:border-zinc-700 dark:bg-zinc-900 dark:text-zinc-400 dark:hover:border-zinc-500",
                    ].join(" ")}
                >
                    {opt.label}
                </button>
            ))}
        </div>
    )
}

function StepperInput({
    id,
    value,
    onChange,
    min,
    max,
    step = 1,
}: {
    id: string
    value: number
    onChange: (v: number) => void
    min: number
    max: number
    step?: number
}) {
    const clamp = (n: number) => Math.min(max, Math.max(min, n))
    return (
        <div className="flex items-center rounded-lg border border-zinc-200 bg-white dark:border-zinc-700 dark:bg-zinc-900 overflow-hidden w-fit">
            <button
                type="button"
                aria-label="Decrease"
                disabled={value <= min}
                onClick={() => onChange(clamp(value - step))}
                className="flex h-9 w-9 items-center justify-center text-zinc-500 hover:bg-zinc-50 disabled:opacity-30 transition-colors dark:hover:bg-zinc-800"
            >
                <svg width="12" height="2" viewBox="0 0 12 2" fill="currentColor">
                    <rect width="12" height="2" rx="1" />
                </svg>
            </button>
            <Input
                id={id}
                type="number"
                min={min}
                max={max}
                step={step}
                value={value}
                onChange={(e) => onChange(clamp(Number(e.target.value)))}
                className="h-9 w-14 border-0 text-center text-sm font-mono shadow-none focus-visible:ring-0 [appearance:textfield] [&::-webkit-inner-spin-button]:appearance-none [&::-webkit-outer-spin-button]:appearance-none"
            />
            <button
                type="button"
                aria-label="Increase"
                disabled={value >= max}
                onClick={() => onChange(clamp(value + step))}
                className="flex h-9 w-9 items-center justify-center text-zinc-500 hover:bg-zinc-50 disabled:opacity-30 transition-colors dark:hover:bg-zinc-800"
            >
                <svg width="12" height="12" viewBox="0 0 12 12" fill="currentColor">
                    <rect x="5" width="2" height="12" rx="1" />
                    <rect y="5" width="12" height="2" rx="1" />
                </svg>
            </button>
        </div>
    )
}

function ColorSwatch({
    id,
    value,
    onChange,
    label,
}: {
    id: string
    value: string
    onChange: (v: string) => void
    label: string
}) {
    return (
        <label
            htmlFor={id}
            className="flex cursor-pointer items-center gap-2.5 rounded-lg border border-zinc-200 bg-white px-3 py-2 transition-colors hover:border-zinc-300 dark:border-zinc-700 dark:bg-zinc-900 dark:hover:border-zinc-500"
        >
            <span
                className="h-5 w-5 shrink-0 rounded-md border border-zinc-300 dark:border-zinc-600"
                style={{ backgroundColor: value }}
                aria-hidden="true"
            />
            <span className="font-mono text-xs text-zinc-600 dark:text-zinc-400">
                {value.toUpperCase()}
            </span>
            <input
                id={id}
                type="color"
                value={value}
                onChange={(e) => onChange(e.target.value)}
                className="sr-only"
                aria-label={label}
            />
            <svg
                className="ml-auto h-3.5 w-3.5 text-zinc-400"
                viewBox="0 0 16 16"
                fill="none"
                stroke="currentColor"
                strokeWidth="1.5"
            >
                <path d="M2 8a6 6 0 1 1 12 0A6 6 0 0 1 2 8z" />
                <path d="M8 5v6M5 8h6" />
            </svg>
        </label>
    )
}

// ── divider ───────────────────────────────────────────────────────────────────

function SectionDivider({ label }: { label: string }) {
    return (
        <div className="flex items-center gap-3 py-1">
            <div className="h-px flex-1 bg-zinc-100 dark:bg-zinc-800" />
            <span className="text-[10px] font-semibold uppercase tracking-widest text-zinc-400">
                {label}
            </span>
            <div className="h-px flex-1 bg-zinc-100 dark:bg-zinc-800" />
        </div>
    )
}

// ── advanced options disclosure ───────────────────────────────────────────────

function AdvancedOptions({
    open,
    onToggle,
    children,
}: {
    open: boolean
    onToggle: () => void
    children: React.ReactNode
}) {
    return (
        <div className="rounded-lg border border-zinc-200 dark:border-zinc-700">
            <button
                type="button"
                aria-expanded={open}
                onClick={onToggle}
                className="flex w-full items-center justify-between px-3.5 py-2.5 text-sm font-medium text-zinc-600 transition-colors hover:text-zinc-900 dark:text-zinc-400 dark:hover:text-zinc-100"
            >
                <span className="flex items-center gap-2">
                    <svg
                        width="14" height="14" viewBox="0 0 16 16"
                        fill="none" stroke="currentColor" strokeWidth="1.5"
                        className="text-zinc-400"
                    >
                        <circle cx="8" cy="8" r="6" />
                        <path d="M6 6.5a2 2 0 0 1 4 0c0 1.5-2 2-2 3.5" />
                        <circle cx="8" cy="12.5" r=".75" fill="currentColor" stroke="none" />
                    </svg>
                    Advanced options
                </span>
                <svg
                    width="14" height="14" viewBox="0 0 16 16"
                    fill="none" stroke="currentColor" strokeWidth="2"
                    className={`transition-transform duration-200 ${open ? "rotate-180" : ""}`}
                >
                    <path d="M3 6l5 5 5-5" strokeLinecap="round" strokeLinejoin="round" />
                </svg>
            </button>

            {/* animated panel — CSS-only height transition via grid trick */}
            <div
                className="grid transition-all duration-200 ease-in-out"
                style={{ gridTemplateRows: open ? "1fr" : "0fr" }}
            >
                <div className="overflow-hidden">
                    <div className="space-y-4 border-t border-zinc-100 px-3.5 py-4 dark:border-zinc-800">
                        {children}
                    </div>
                </div>
            </div>
        </div>
    )
}

// ── main form ─────────────────────────────────────────────────────────────────

export function QrForm({
    onSubmit,
}: {
    onSubmit?: (values: QrFormValues) => void
}) {
    const [values, setValues] = useState<QrFormValues>(DEFAULT)
    const [urlError, setUrlError] = useState<string | null>(null)
    const [showAdvanced, setShowAdvanced] = useState(false)

    const set = <K extends keyof QrFormValues>(key: K, val: QrFormValues[K]) =>
        setValues((prev) => ({ ...prev, [key]: val }))

    const validateUrl = (raw: string) => {
        if (!raw) return "URL is required."
        try {
            const u = new URL(raw.startsWith("http") ? raw : `https://${raw}`)
            if (!["http:", "https:"].includes(u.protocol))
                return "Only http/https URLs are supported."
        } catch {
            return "Please enter a valid URL."
        }
        return null
    }

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault()
        const err = validateUrl(values.url)
        setUrlError(err)
        if (err) return

        const { scale, ...rest } = values
        const payload = values.format === "SVG" ? rest : values

        onSubmit?.(payload as QrFormValues)
    }

    return (
        <form onSubmit={handleSubmit} noValidate>
            <FieldSet className="w-full max-w-sm space-y-5">
                {/* ── header ── */}
                <div>
                    <FieldLegend className="text-lg font-semibold tracking-tight">
                        Generate QR Code
                    </FieldLegend>
                    <FieldDescription className="mt-0.5 text-sm text-zinc-500">
                        Encode any URL as a PNG or SVG.
                    </FieldDescription>
                </div>

                <FieldGroup className="space-y-4">
                    {/* ── URL ── */}
                    <Field>
                        <FieldLabel htmlFor="qr-url">
                            URL{" "}
                            <span className="ml-1 rounded bg-zinc-100 px-1 py-0.5 text-[10px] font-semibold uppercase tracking-wide text-zinc-500 dark:bg-zinc-800 dark:text-zinc-400">
                                required
                            </span>
                        </FieldLabel>
                        <div className="relative">
                            <Input
                                id="qr-url"
                                type="url"
                                placeholder="https://example.com"
                                value={values.url}
                                autoComplete="url"
                                aria-describedby={urlError ? "qr-url-error" : undefined}
                                aria-invalid={!!urlError}
                                onChange={(e) => {
                                    set("url", e.target.value)
                                    if (urlError) setUrlError(validateUrl(e.target.value))
                                }}
                                onBlur={() => setUrlError(validateUrl(values.url))}
                                className={urlError ? "border-red-400 focus-visible:ring-red-300" : ""}
                            />
                        </div>
                        {urlError && (
                            <p id="qr-url-error" role="alert" className="mt-1 text-xs text-red-500">
                                {urlError}
                            </p>
                        )}
                    </Field>

                    {/* ── Format ── */}
                    <Field>
                        <FieldLabel>
                            Format{" "}
                            <span className="ml-1 rounded bg-zinc-100 px-1 py-0.5 text-[10px] font-semibold uppercase tracking-wide text-zinc-500 dark:bg-zinc-800 dark:text-zinc-400">
                                required
                            </span>
                        </FieldLabel>
                        <SegmentedControl<OutputFormat>
                            label="Output format"
                            value={values.format}
                            onChange={(v) => set("format", v)}
                            options={[
                                { value: "PNG", label: "PNG", hint: "Raster image" },
                                { value: "SVG", label: "SVG", hint: "Scalable vector" },
                            ]}
                        />
                    </Field>

                    {/* ── Advanced options ── */}
                    <AdvancedOptions
                        open={showAdvanced}
                        onToggle={() => setShowAdvanced((v) => !v)}
                    >
                        <SectionDivider label="Encoding" />

                        {/* ── Error correction ── */}
                        <Field>
                            <FieldLabel>Error Correction</FieldLabel>
                            <FieldDescription className="mb-2">
                                Higher levels let the QR survive more damage, but increase density.
                            </FieldDescription>
                            <SegmentedControl<Ecc>
                                label="Error correction level"
                                value={values.ecc}
                                onChange={(v) => set("ecc", v)}
                                options={[
                                    { value: "Low", label: "L · 7%", hint: "Low — 7% recovery" },
                                    { value: "Medium", label: "M · 15%", hint: "Medium — 15% recovery (default)" },
                                    { value: "Quartile", label: "Q · 25%", hint: "Quartile — 25% recovery" },
                                    { value: "High", label: "H · 30%", hint: "High — 30% recovery" },
                                ]}
                            />
                        </Field>

                        <SectionDivider label="Output" />

                        {/* ── Scale (PNG only) ── */}
                        {values.format === "PNG" && (
                            <Field>
                                <FieldLabel htmlFor="qr-scale">Scale</FieldLabel>
                                <FieldDescription className="mb-2">
                                    Pixels per module (1 module = 1 QR cell).
                                </FieldDescription>
                                <StepperInput
                                    id="qr-scale"
                                    value={values.scale}
                                    onChange={(v) => set("scale", v)}
                                    min={1}
                                    max={20}
                                />
                            </Field>
                        )}

                        {/* ── Border ── */}
                        <Field>
                            <FieldLabel htmlFor="qr-border">
                                Quiet Zone{" "}
                                <span className="ml-1 text-xs font-normal text-zinc-400">
                                    (border modules)
                                </span>
                            </FieldLabel>
                            <FieldDescription className="mb-2">
                                ISO recommends at least 4 modules of white space.
                            </FieldDescription>
                            <StepperInput
                                id="qr-border"
                                value={values.border}
                                onChange={(v) => set("border", v)}
                                min={0}
                                max={16}
                            />
                        </Field>

                        <SectionDivider label="Appearance" />

                        {/* ── Colors ── */}
                        <div className="grid grid-cols-2 gap-3">
                            <Field>
                                <FieldLabel htmlFor="qr-dark">Dark modules</FieldLabel>
                                <ColorSwatch
                                    id="qr-dark"
                                    label="Dark module color"
                                    value={values.darkColor}
                                    onChange={(v) => set("darkColor", v)}
                                />
                            </Field>
                            <Field>
                                <FieldLabel htmlFor="qr-light">Light modules</FieldLabel>
                                <ColorSwatch
                                    id="qr-light"
                                    label="Light module color"
                                    value={values.lightColor}
                                    onChange={(v) => set("lightColor", v)}
                                />
                            </Field>
                        </div>
                    </AdvancedOptions>
                </FieldGroup>

                {/* ── submit ── */}
                <button
                    type="submit"
                    className="w-full rounded-lg bg-zinc-900 px-4 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-zinc-700 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-zinc-900 focus-visible:ring-offset-2 active:bg-zinc-800 dark:bg-zinc-100 dark:text-zinc-900 dark:hover:bg-zinc-300"
                >
                    Generate QR Code
                </button>
            </FieldSet>
        </form>
    )
}
