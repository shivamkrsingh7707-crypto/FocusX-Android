import { View, Text, ScrollView } from "react-native";
import { BarChart3 } from "lucide-react-native";
import { colors } from "../../theme";

export default function AnalyticsScreen() {
  return (
    <ScrollView
      className="flex-1 bg-amoled"
      contentContainerStyle={{ paddingTop: 60, paddingHorizontal: 16, paddingBottom: 100 }}
    >
      <View className="flex-row items-center gap-3 mb-8">
        <BarChart3 size={28} color={colors.primary} />
        <Text className="text-2xl font-bold text-white">Analytics Vault</Text>
      </View>

      <View className="bg-card rounded-2xl p-5 border border-card-border mb-4">
        <Text className="text-white font-semibold text-lg mb-2">Progress</Text>
        <Text className="text-text-secondary text-sm">Track your daily and weekly study hours.</Text>
      </View>

      <View className="bg-card rounded-2xl p-5 border border-card-border mb-4">
        <Text className="text-white font-semibold text-lg mb-2">Test Scores</Text>
        <Text className="text-text-secondary text-sm">Visualize your academic progress over time.</Text>
      </View>

      <View className="bg-card rounded-2xl p-5 border border-card-border">
        <View className="flex-row items-center gap-2">
          <Text className="text-2xl">🔥</Text>
          <Text className="text-white font-semibold text-lg">Streak</Text>
        </View>
        <Text className="text-text-secondary text-sm mt-1">0 days consecutive study.</Text>
      </View>
    </ScrollView>
  );
}
