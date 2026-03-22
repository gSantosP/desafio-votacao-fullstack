import { Routes, Route } from 'react-router-dom';
import Layout from './components/Layout';
import HomePage from './pages/HomePage';
import AgendaDetailPage from './pages/AgendaDetailPage';

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Layout />}>
        <Route index element={<HomePage />} />
        <Route path="agendas/:id" element={<AgendaDetailPage />} />
      </Route>
    </Routes>
  );
}
