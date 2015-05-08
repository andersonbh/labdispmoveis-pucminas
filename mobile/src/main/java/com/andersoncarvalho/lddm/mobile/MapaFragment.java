package com.andersoncarvalho.lddm.mobile;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by anderson on 08/05/15.
 */

public class MapaFragment extends Fragment implements View.OnClickListener, ConnectionCallbacks,
        OnConnectionFailedListener,
        LocationListener,SensorEventListener {

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String LOG = "LDDM";
    private SensorManager sensorManager;
    private Sensor sensor;
    // Gravity force on x, y, z axis
    private float gravity[] = new float[3];

    private int counter;
    private long ultimoMovimento = 0;
    private int idMovimento = 1;

    private static final float ALPHA = 0.8f;
    private static final int THRESHOLD = 7;
    private static final int SHAKE_INTERVAL = 500; // ms
    private static final int COUNTS = 2;
    public GoogleMap mMap;
    MapView mMapView;
    int ZOOM = 18, BEARING;
    private CameraPosition cp;
    private static View v;
    LatLng posicao;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    Location localizacao;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        callConnection();
        if (v != null) {
            ViewGroup parent = (ViewGroup) v.getParent();
            if (parent != null) {
                parent.removeView(v);
            }
        }
        try {
            v = inflater.inflate(R.layout.mapa_fragment, container, false);
        } catch (InflateException e) {
        }
        //Inicia o MapView para dar suporte ao mapa google maps
        mMapView = (MapView) v.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mMap = mMapView.getMap();
        //Monta o mapa personalizado.
        montarMapa();
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        return v;
    }


    public void zoomTop(LatLng posicao, int zoom) {
        if (cp == null) {
            cp = new CameraPosition.Builder().target(posicao).zoom(zoom).bearing(BEARING).tilt(0).build();
        }
        CameraUpdate update = CameraUpdateFactory.newCameraPosition(cp);
        mMap.animateCamera(update, 2000, null);
        mMap.moveCamera(update);
        cp = null;

        mMap.setBuildingsEnabled(true);
    }

    public static Bitmap createDrawableFromView(Context context, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

    public void criarMarcadorShake() {
        View marker = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout, null);
        DateFormat dataLoc = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        mMap.addMarker(new MarkerOptions().
                position(new LatLng(localizacao.getLatitude(), localizacao.getLongitude())).
                title("Movimento brusco " + idMovimento).
                snippet("Horário: " + dataLoc.format(new Date(localizacao.getTime())) +
                        "\nPrecisão: " + localizacao.getAccuracy()).
                        alpha(0.97f).
                        icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this.getActivity(), marker))));
        idMovimento++;
    }

    public void montarMapa() {
            if (mMap != null) {
                //limpar markers do mapa na troca de fragment
                limparMarcadores();
                //Habilitar localizacao do usuario - desabilitado pois utiliza botao customizado
                mMap.setMyLocationEnabled(true);
                // Setar controles de zoom in e zoom out
                mMap.getUiSettings().setZoomControlsEnabled(true);
                //toolbar de ir para desativado.
                mMap.getUiSettings().setMapToolbarEnabled(false);
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                //Habilitar botao de compasso
                mMap.getUiSettings().setCompassEnabled(true);

                setarPosicaoInicial();

                mMap.setBuildingsEnabled(true);
                /*
                3000 = milisegundos para exibição da animação do mapa.
                 */
                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                    }
                });
            }
    }

    public void limparMarcadores() {
        if (mMap != null) {
            mMap.clear();
        }
    }

    public void setarPosicaoInicial() {
        /*
                Localização padrao quando o app é aberto
                 */
            posicao = new LatLng(-19.923605, -43.992537);
            BEARING = 309;
            ZOOM = 13;
                /*
                zoom = zoom que será setado no mapa no incio da aplicação
                tilt = angulo "3D" do mapa
                 */
        if (cp == null) {
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(posicao).zoom(ZOOM).bearing(BEARING).tilt(0).build());
            mMap.animateCamera(update, 2000, null);
            mMap.moveCamera(update);
        }
    }

    @Override
    public void onClick(View v) {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLocationChanged(Location location) {
        localizacao = location;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            startLocationUpdate();
        }
        if (cp != null) {
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cp));
            cp = null;
        }
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        if(mMap != null) {
            cp = mMap.getCameraPosition();
        }

        if (mGoogleApiClient != null) {
            stopLocationUpdate();
        }
        sensorManager.unregisterListener(this);

    }

    private synchronized void callConnection() {
        Log.i(LOG, "callConnection()");
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }


    private void initLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(2000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    private void startLocationUpdate() {
        initLocationRequest();
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }


    private void stopLocationUpdate() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(LOG, "onConnected(" + bundle + ")");
        Location l = LocationServices
                .FusedLocationApi
                .getLastLocation(mGoogleApiClient);
        if (l != null) {
            Log.i(LOG, "latitude: " + l.getLatitude());
            Log.i(LOG, "longitude: " + l.getLongitude());
        }

        startLocationUpdate();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(LOG, "onConnectionSuspended(" + i + ")");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(LOG, "onConnectionFailed(" + connectionResult + ")");
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    public void MostrarDistancia(LatLng position, String nomMarker) {
        Location temp = new Location("temp");
        temp.setLatitude(position.latitude);
        temp.setLongitude(position.longitude);
        int distancia = distanciaAproximadaemMetros(temp);
        if (distancia != -1) {
            Toast toast = Toast.makeText(getActivity(), "Você está a aproximadamente " + distancia + " metros do marker " + nomMarker, Toast.LENGTH_LONG);
            toast.show();
        } else {
            Toast toast = Toast.makeText(getActivity(), "Não foi possível calcular sua distância até o marker " + nomMarker, Toast.LENGTH_LONG);
            toast.show();
        }
    }

    public int distanciaAproximadaemMetros(Location local) {
        if(localizacao != null) {
                return (int) localizacao.distanceTo(local);
            } else {
                return -1;
            }

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float acceleration = maxAcceleration(sensorEvent);
        Sensor mySensor = sensorEvent.sensor;
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            if (Math.abs(acceleration) >= THRESHOLD) {
                long agora = System.currentTimeMillis();
                if ((agora - ultimoMovimento) > SHAKE_INTERVAL) {
                    ultimoMovimento = agora;
                    criarMarcadorShake();
                } else
                    ultimoMovimento = System.currentTimeMillis();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private float maxAcceleration(SensorEvent event) {
        // Low-pass filter
        gravity[0] = ALPHA * gravity[0] + (1 - ALPHA) * event.values[0]; // x axis
        gravity[1] = ALPHA * gravity[1] + (1 - ALPHA) * event.values[1]; // y axis
        gravity[2] = ALPHA * gravity[2] + (1 - ALPHA) * event.values[2]; // z axis

        // High-pass filter
        float linear_acceleration[] = new float[3];
        linear_acceleration[0] = event.values[0] - gravity[0];
        linear_acceleration[1] = event.values[1] - gravity[1];
        linear_acceleration[2] = event.values[2] - gravity[2];
        float max = Math.max(Math.max(linear_acceleration[0], linear_acceleration[1])
                ,linear_acceleration[2]);

        if (max == linear_acceleration[1]) {
            return linear_acceleration[1];
        } else {
            return 0.0f;
        }
    } // Go to onSensorChanged method above
}