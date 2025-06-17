// src/components/theme-provider.tsx
import { ThemeProvider as NextThemesProvider, type ThemeProviderProps } from "next-themes"

export function ThemeProvider({ children, ...props }: ThemeProviderProps) {
  return (
    <NextThemesProvider
      defaultTheme="system"
      attribute="class"
      enableSystem
      {...props}
    >
      {children}
    </NextThemesProvider>
  )
}