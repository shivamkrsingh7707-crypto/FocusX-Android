import { View, Text, ScrollView, Switch } from "react-native";
import { useState } from "react";
import { Settings, Moon, Sun } from "lucide-react-native";
import { colors } from "../../theme";

export default function SettingsScreen() {
  const [isDark, setIsDark] = useState(true);

  return (
    <ScrollView
      className="flex-1 bg-amoled"
      contentContainerStyle={{ paddingTop: 60, paddingHorizontal: 16, paddingBottom: 100 }}
    >
      <View className="flex-row items-center gap-3 mb-8">
        <Settings size={28} color={colors.primary} />
        <Text className="text-2xl font-bold text-white">Settings</Text>
      </View>

      <View className="bg-card rounded-2xl p-5 border border-card-border">
        <View className="flex-row items-center justify-between">
          <View className="flex-row items-center gap-3">
            {isDark ? (
              <Moon size={20} color={colors.primary} />
            ) : (
              <Sun size={20} color={colors.warning} />
            )}
            <View>
              <Text className="text-white font-medium">Dark Mode</Text>
              <Text className="text-text-secondary text-xs">AMOLED optimized</Text>
            </View>
          </View>
          <Switch
            value={isDark}
            onValueChange={setIsDark}
            trackColor={{ false: "#3A3A3C", true: colors.primary }}
            thumbColor="#FFFFFF"
          />
        </View>
      </View>
    </ScrollView>
  );
}
