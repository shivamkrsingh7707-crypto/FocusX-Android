import { Tabs } from "expo-router";
import { Platform, View } from "react-native";
import { BlurView } from "expo-blur";
import { Compass, BookOpen, BarChart3, Settings } from "lucide-react-native";
import { colors } from "../../theme";

function TabIcon({ icon: Icon, focused }: { icon: any; focused: boolean }) {
  return (
    <Icon
      size={24}
      color={focused ? colors.primary : colors.textSecondary}
    />
  );
}

export default function TabLayout() {
  return (
    <Tabs
      screenOptions={{
        headerShown: false,
        tabBarStyle: {
          position: "absolute",
          bottom: 0,
          left: 0,
          right: 0,
          height: Platform.OS === "ios" ? 88 : 68,
          backgroundColor: "transparent",
          borderTopWidth: 0,
          elevation: 0,
        },
        tabBarBackground: () => (
          <BlurView
            intensity={80}
            tint="dark"
            style={{
              position: "absolute",
              top: 0,
              left: 0,
              right: 0,
              bottom: 0,
              borderTopWidth: 1,
              borderTopColor: colors.cardBorder,
              backgroundColor: "rgba(0,0,0,0.5)",
            }}
          />
        ),
        tabBarActiveTintColor: colors.primary,
        tabBarInactiveTintColor: colors.textSecondary,
        tabBarShowLabel: true,
        tabBarLabelStyle: {
          fontFamily: Platform.OS === "ios" ? "System" : "Roboto",
          fontSize: 11,
          fontWeight: "600",
          marginTop: 2,
        },
      }}
    >
      <Tabs.Screen
        name="index"
        options={{
          title: "Focus Hub",
          tabBarIcon: ({ focused }) => (
            <TabIcon icon={Compass} focused={focused} />
          ),
        }}
      />
      <Tabs.Screen
        name="knowledge"
        options={{
          title: "Knowledge",
          tabBarIcon: ({ focused }) => (
            <TabIcon icon={BookOpen} focused={focused} />
          ),
        }}
      />
      <Tabs.Screen
        name="analytics"
        options={{
          title: "Analytics",
          tabBarIcon: ({ focused }) => (
            <TabIcon icon={BarChart3} focused={focused} />
          ),
        }}
      />
      <Tabs.Screen
        name="settings"
        options={{
          title: "Settings",
          tabBarIcon: ({ focused }) => (
            <TabIcon icon={Settings} focused={focused} />
          ),
        }}
      />
    </Tabs>
  );
}
