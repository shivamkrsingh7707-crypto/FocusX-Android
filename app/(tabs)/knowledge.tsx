import { View, Text, ScrollView } from "react-native";
import { BookOpen } from "lucide-react-native";
import { colors } from "../../theme";

export default function KnowledgeScreen() {
  return (
    <ScrollView
      className="flex-1 bg-amoled"
      contentContainerStyle={{ paddingTop: 60, paddingHorizontal: 16, paddingBottom: 100 }}
    >
      <View className="flex-row items-center gap-3 mb-8">
        <BookOpen size={28} color={colors.primary} />
        <Text className="text-2xl font-bold text-white">Knowledge Base</Text>
      </View>

      <View className="bg-card rounded-2xl p-5 border border-card-border mb-4">
        <Text className="text-white font-semibold text-lg mb-2">Notes</Text>
        <Text className="text-text-secondary text-sm">Create and organize your study notes with tags.</Text>
      </View>

      <View className="bg-card rounded-2xl p-5 border border-card-border">
        <Text className="text-white font-semibold text-lg mb-2">Active Recall</Text>
        <Text className="text-text-secondary text-sm">Convert notes into flashcards and quiz yourself.</Text>
      </View>
    </ScrollView>
  );
}
