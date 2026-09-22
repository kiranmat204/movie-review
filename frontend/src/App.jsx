import { Routes, Route } from "react-router-dom";
import MovieListPage from "./pages/MovieListPage";
import MovieDetailsPage from "./pages/MovieDetailsPage";

function App() {
  return (
    <Routes>
      <Route path="/" element={<MovieListPage />} />

      <Route
        path="/movies/:movieId"
        element={<MovieDetailsPage />}
      />
    </Routes>
  );
}

export default App;