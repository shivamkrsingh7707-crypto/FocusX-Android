/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./app/**/*.{ts,tsx}", "./components/**/*.{ts,tsx}"],
  presets: [require("nativewind/preset")],
  theme: {
    extend: {
      colors: {
        amoled: "#000000",
        card: "#121212",
        "card-elevated": "#1C1C1E",
        "card-border": "#2C2C2E",
        primary: "#9D4EDD",
        "primary-glow": "#C77DFF",
        success: "#52B788",
        warning: "#FF6B6B",
        warningglow: "#FF8A8A",
        "text-primary": "#FFFFFF",
        "text-secondary": "#8E8E93",
        "text-tertiary": "#636366",
      },
      fontFamily: {
        mono: ["SpaceMono", "monospace"],
      },
    },
  },
  plugins: [],
};
