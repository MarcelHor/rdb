import { useState } from "react";
import { Form, Button, Alert } from "react-bootstrap";
import { WeatherControllerApi } from "../api/build";
import QueryResultModal from "./QueryResultModal";

export type EndpointOption =
  | "getCitiesWithRain"
  | "getCityWithMaxTempDiff"
  | "getStableWeatherDays";

const api = new WeatherControllerApi();

export default function Forms({ selected }: { selected: EndpointOption }) {
  const [form, setForm] = useState<any>({});
  const [error, setError] = useState("");
  const [response, setResponse] = useState<any>(null);

  const handleChange = (e: React.ChangeEvent<any>) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const requiredFields = {
      getCitiesWithRain: ["intensity", "from", "to"],
      getCityWithMaxTempDiff: ["date"],
      getStableWeatherDays: ["city", "weatherType", "from", "to"],
    }[selected];

    const missing = requiredFields.filter((f) => !form[f]);
    if (missing.length > 0) {
      setError(`Chybí pole: ${missing.join(", ")}`);
      return;
    }

    setError("");
    try {
      let result;
      switch (selected) {
        case "getCitiesWithRain":
          result = await api.getCitiesWithRain(
            Number(form.intensity),
            form.from,
            form.to
          );
          break;
        case "getCityWithMaxTempDiff":
          result = await api.getCityWithMaxTempDiff(form.date);
          break;
        case "getStableWeatherDays":
          result = await api.getStableWeatherDays(
            form.city,
            form.weatherType,
            form.from,
            form.to
          );
          break;
      }
      setResponse(result.data);
    } catch (err: any) {
      setError("Chyba při volání API.");
      setResponse(null);
      console.error(err);
    }
  };

  const renderFields = () => {
    switch (selected) {
      case "getCitiesWithRain":
        return (
          <>
            <Form.Group>
              <Form.Label>Intenzita</Form.Label>
              <Form.Control
                name="intensity"
                type="float"
                onChange={handleChange}
                required
              />
            </Form.Group>
            <Form.Group>
              <Form.Label>Od</Form.Label>
              <Form.Control
                name="from"
                type="date"
                onChange={handleChange}
                required
              />
            </Form.Group>
            <Form.Group>
              <Form.Label>Do</Form.Label>
              <Form.Control
                name="to"
                type="date"
                onChange={handleChange}
                required
              />
            </Form.Group>
          </>
        );
      case "getCityWithMaxTempDiff":
        return (
          <Form.Group>
            <Form.Label>Datum</Form.Label>
            <Form.Control
              name="date"
              type="date"
              onChange={handleChange}
              required
            />
          </Form.Group>
        );
      case "getStableWeatherDays":
        return (
          <>
            <Form.Group>
              <Form.Label>Město</Form.Label>
              <Form.Control name="city" onChange={handleChange} required />
            </Form.Group>
            <Form.Group>
              <Form.Label>Typ počasí</Form.Label>
              <Form.Control
                name="weatherType"
                onChange={handleChange}
                required
              />
            </Form.Group>
            <Form.Group>
              <Form.Label>Od</Form.Label>
              <Form.Control
                name="from"
                type="date"
                onChange={handleChange}
                required
              />
            </Form.Group>
            <Form.Group>
              <Form.Label>Do</Form.Label>
              <Form.Control
                name="to"
                type="date"
                onChange={handleChange}
                required
              />
            </Form.Group>
          </>
        );
    }
  };

  return (
    <Form onSubmit={handleSubmit} className="mt-3">
      {renderFields()}
      {error && (
        <Alert variant="danger" className="mt-3">
          {error}
        </Alert>
      )}
      <Button type="submit" className="mt-3">
        Odeslat
      </Button>

      <QueryResultModal
        show={response}
        onHide={() => setResponse(null)}
        result={response}
      />
    </Form>
  );
}
