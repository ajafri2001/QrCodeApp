import { SignupForm } from "@/components/signup-form";

/**
 * Signup page wrapper.
 * Centers the signup form vertically and horizontally.
 */
export default function SignUp() {
    return (
        <div className="flex min-h-svh w-full items-center justify-center p-6 md:p-10">
            <div className="w-full max-w-sm">
                <SignupForm />
            </div>
        </div>
    )
}
