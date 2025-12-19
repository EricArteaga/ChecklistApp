import React from 'react' // React y hooks para el componente funcional
import { View, Text, Button } from 'react-native' // componentes básicos de UI para React Native

export default function App() { // componente raíz de la app móvil
  const [view, setView] = React.useState('checklist') // estado local que selecciona la vista actual
  return (
    <View style={{ flex: 1, padding: 16 }}> {/* contenedor principal con padding */}
      <Text style={{ fontSize: 20, marginBottom: 12 }}>ChecklistApp — Móvil</Text> {/* título */}
      <View style={{ flexDirection: 'row', marginBottom: 12 }}> {/* fila de botones para cambiar vistas */}
        <Button title="Checklist" onPress={() => setView('checklist')} /> {/* botón para vista checklist */}
        <View style={{ width: 8 }} /> {/* separador horizontal */}
        <Button title="Heatmap" onPress={() => setView('heatmap')} /> {/* botón para vista heatmap */}
      </View>
      {view === 'checklist' ? (
        <Text>Checklist (TODO: implementar vista móvil)</Text> /* marcador de vista checklist móvil */
      ) : (
        <Text>Heatmap (TODO: implementar vista móvil)</Text> /* marcador de vista heatmap móvil */
      )}
    </View>
  )
}
