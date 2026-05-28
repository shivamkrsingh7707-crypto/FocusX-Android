export const colors = {
  amoled: "#000000",
  card: "#121212",
  cardElevated: "#1C1C1E",
  cardBorder: "#2C2C2E",
  primary: "#9D4EDD",
  primaryGlow: "#C77DFF",
  success: "#52B788",
  warning: "#FF6B6B",
  warningGlow: "#FF8A8A",
  textPrimary: "#FFFFFF",
  textSecondary: "#8E8E93",
  textTertiary: "#636366",
} as const;

export const spacing = {
  xs: 4,
  sm: 8,
  md: 16,
  lg: 24,
  xl: 32,
} as const;

export const borderRadius = {
  sm: 8,
  md: 12,
  lg: 16,
  xl: 24,
  full: 9999,
} as const;

export const spring = {
  damping: 15,
  stiffness: 150,
  mass: 1,
} as const;

export const hapticPresets = {
  light: "light" as const,
  medium: "medium" as const,
  heavy: "heavy" as const,
  success: "success" as const,
  warning: "warning" as const,
  error: "error" as const,
};
