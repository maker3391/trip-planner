import React, { useEffect } from "react";
import {
  useMap,
  useMapsLibrary,
  Marker,
  Polyline,
} from "@vis.gl/react-google-maps";
import { PlacePoint } from "../../types/map";
import { SearchPlace } from "../../types/searchPlace.ts";

interface MapControllerProps {
  searchKeyword: string;
  setSearchResults: React.Dispatch<React.SetStateAction<SearchPlace[]>>;
  openSearchModal: () => void;
  selectedSearchPlace: SearchPlace | null;
  clearSelectedSearchPlace: () => void;
  path: PlacePoint[];
  connections: any[];
  onMapClick: (point: PlacePoint) => void;
  onSelect: (idx: number) => void;
  selectedSource: number | null;
  onPhotosRestored?: (idx: number, photos: string[]) => void;
  lineColor: string;
  pinColor: string;
  selectedPinColor: string;
}

const normalizeText = (text: string) =>
  text.replace(/\s+/g, "").trim().toLowerCase();

const dedupePlaces = (places: SearchPlace[]) => {
  const seenPlaceIds = new Set<string>();
  const seenNames = new Set<string>();

  return places.filter((place) => {
    const normalizedName = normalizeText(place.name);

    if (place.placeId && seenPlaceIds.has(place.placeId)) {
      return false;
    }

    if (seenNames.has(normalizedName)) {
      return false;
    }

    if (place.placeId) {
      seenPlaceIds.add(place.placeId);
    }

    seenNames.add(normalizedName);
    return true;
  });
};

export default function MapController({
  searchKeyword,
  setSearchResults,
  openSearchModal,
  selectedSearchPlace,
  clearSelectedSearchPlace,
  path,
  connections,
  onMapClick,
  onSelect,
  selectedSource,
  onPhotosRestored,
  lineColor,
  pinColor,
  selectedPinColor,
}: MapControllerProps) {
  const map = useMap();
  const placesLib = useMapsLibrary("places");

  useEffect(() => {
    if (!map || !placesLib || !searchKeyword) {
      return;
    }

    const service = new google.maps.places.PlacesService(
      document.createElement("div")
    );

    service.textSearch(
      {
        query: searchKeyword,
        language: "ko",
      },
      (results, status) => {
        if (
          status !== google.maps.places.PlacesServiceStatus.OK ||
          !results ||
          results.length === 0
        ) {
          setSearchResults([]);
          alert("검색 결과가 없습니다.");
          return;
        }

        const mappedResults: SearchPlace[] = results
          .filter((place) => place.geometry?.location)
          .map((place) => ({
            placeId: place.place_id,
            name: place.name || searchKeyword,
            address: place.formatted_address || place.vicinity || "",
            lat: Number(place.geometry!.location!.lat()),
            lng: Number(place.geometry!.location!.lng()),
          }));

        const uniqueResults = dedupePlaces(mappedResults);
        setSearchResults(uniqueResults);

        const normalizedKeyword = normalizeText(searchKeyword);
        const exactMatch = uniqueResults.find(
          (place) => normalizeText(place.name) === normalizedKeyword
        );

        if (exactMatch) {
          map.panTo({ lat: exactMatch.lat, lng: exactMatch.lng });
          map.setZoom(15);
          return;
        }

        openSearchModal();
      }
    );
  }, [map, placesLib, searchKeyword, setSearchResults, openSearchModal]);

  useEffect(() => {
    if (!map || !selectedSearchPlace) return;

    map.setCenter({
      lat: selectedSearchPlace.lat,
      lng: selectedSearchPlace.lng,
    });
    map.setZoom(21);

    clearSelectedSearchPlace();
  }, [map, selectedSearchPlace, clearSelectedSearchPlace]);

  useEffect(() => {
    if (!map) return;

    const clickListener = map.addListener("click", (e: google.maps.MapMouseEvent) => {
      const poiEvent = e as google.maps.IconMouseEvent;
      if (poiEvent.placeId) poiEvent.stop();

      if (!e.latLng) return;

      const currentLat = Number(e.latLng.lat());
      const currentLng = Number(e.latLng.lng());
      const service = new google.maps.places.PlacesService(document.createElement("div"));

      if (poiEvent.placeId) {
        service.getDetails(
          {
            placeId: poiEvent.placeId,
            fields: ["name", "formatted_address", "photos", "place_id"],
            language: "ko",
          },
          (place, status) => {
            if (status === google.maps.places.PlacesServiceStatus.OK && place) {
              onMapClick({
                lat: currentLat,
                lng: currentLng,
                name: place.name || "알 수 없는 장소",
                address: place.formatted_address || "",
                placeId: place.place_id,
                photos: place.photos?.map((p) => p.getUrl({ maxWidth: 400 })),
                isMemoOpen: true,
              });
            }
          }
        );
      } else {
        const geocoder = new google.maps.Geocoder();
        geocoder.geocode(
          {
            location: { lat: currentLat, lng: currentLng },
            language: "ko",
          },
          (results, status) => {
            if (status === google.maps.GeocoderStatus.OK && results?.[0]) {
              const fullAddress = results[0].formatted_address;
              const addressParts = fullAddress.split(" ");

              const displayName =
                addressParts.length > 2
                  ? `${addressParts[addressParts.length - 2]} ${addressParts[addressParts.length - 1]}`
                  : fullAddress;

              onMapClick({
                lat: currentLat,
                lng: currentLng,
                name: displayName,
                address: fullAddress,
                placeId: results[0].place_id,
                isMemoOpen: true,
              });
            } else {
              onMapClick({
                lat: currentLat,
                lng: currentLng,
                name: "지정된 위치",
                address: "상세 주소 정보를 불러올 수 없습니다.",
                isMemoOpen: true,
              });
            }
          }
        );
      }
    });

    return () => google.maps.event.removeListener(clickListener);
  }, [map, onMapClick]);

  useEffect(() => {
    if (!map || !placesLib || !path || path.length === 0) return;

    const service = new google.maps.places.PlacesService(
      document.createElement("div")
    );

    path.forEach((point, index) => {
      if (point.placeId && (!point.photos || point.photos.length === 0)) {
        service.getDetails(
          {
            placeId: point.placeId,
            fields: ["photos"],
          },
          (place, status) => {
            if (
              status === google.maps.places.PlacesServiceStatus.OK &&
              place?.photos &&
              onPhotosRestored
            ) {
              const restoredPhotos = place.photos.map((p: any) =>
                p.getUrl({ maxWidth: 400 })
              );
              onPhotosRestored(index, restoredPhotos);
            }
          }
        );
      }
    });
  }, [map, placesLib, path, onPhotosRestored]);

  return (
    <>
      {path.map((point, i) => (
        <React.Fragment key={`${i}-${point.lat}-${point.lng}`}>
          <Marker
            position={{ lat: point.lat, lng: point.lng }}
            label={{
              text: (i + 1).toString(),
              color: "white",
              fontWeight: "bold",
            }}
            icon={{
              path: google.maps.SymbolPath.BACKWARD_CLOSED_ARROW,
              fillColor: selectedSource === i ? selectedPinColor : pinColor,
              fillOpacity: 1,
              strokeWeight: 2,
              strokeColor: "#FFFFFF",
              scale: 8,
            }}
            zIndex={selectedSource === i ? 1000 : i}
            onClick={() => onSelect(i)}
          />

        </React.Fragment>
      ))}

      {connections.map(
        (conn, i) =>
          path[conn.from] &&
          path[conn.to] && (
            <Polyline
              key={i}
              path={[path[conn.from], path[conn.to]]}
              strokeColor={lineColor}
              strokeWeight={4}
              strokeOpacity={0.8}
            />
          )
      )}
    </>
  );
}