import { View, Text, ScrollView } from "react-native";
import { Compass } from "lucide-react-native";
import { colors } from "../../theme";

export default function FocusHubScreen() {
  return (
    <ScrollView
      className="flex-1 bg-amoled"
      contentContainerStyle={{ paddingTop: 60, paddingHorizontal: 16, paddingBottom: 100 }}
    >
      <View className="flex-row items-center gap-3 mb-8">
        <Compass size={28} color={colors.primary} />
        <Text className="text-2xl font-bold text-white">Focus Hub</Text>
      </View>

      <View className="bg-card rounded-2xl p-6 border border-card-border items-center mb-4">
        <Text className="text-text-secondary text-sm mb-4">Study Timer</Text>
        <View className="w-48 h-48 rounded-full border-4 border-primary items-center justify-center mb-6">
          <Text className="text-5xl font-mono text-white">25:00</Text>
        </View>
        <View className="bg-primary rounded-xl px-8 py-3">
          <Text className="text-white font-semibold text-base">Start Focus</Text>
        </View>
      </View>

      <View className="bg-card rounded-2xl p-5 border border-card-border">
        <View className="flex-row items-center gap-2 mb-3">
          <View className="w-2 h-2 rounded-full bg-success" />
          <Text className="text-text-secondary text-sm">Today's Focus</Text>
        </View>
        <Text className="text-3xl font-bold text-white">0<Text className="text-lg text-text-secondary"> min</Text></Text>
      </View>
    </ScrollView>
  );
}
