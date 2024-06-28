RWStructuredBuffer<float4> Output : register( b0 );

struct Sphere {
    double3 origin;
    double radius;
    double4 color;
};

RWStructuredBuffer<Sphere> spheres : register( b1 );

cbuffer VariablesBuffer : register( b2 ) {
    float width;
    float height;
};

struct RayHit {
    Sphere sphere;
    double distance;
};

double getClosestHitDistance(double3 origin, double3 direction, double radius) {
    double a = dot(direction, direction);
    double b = 2 * dot(origin, direction);
    double c = dot(origin, origin) - radius * radius;

    double nominator = b * b - 4 * a * c;

    if (nominator < 0) {
        return -1;
    }

    nominator = -b - sqrt(nominator);

    return nominator / (2 * a);
}

RayHit getClosestRayHit(double3 rayDirection) {
    double closestHitDistance = 1.0 / .0;
    RayHit rayHit;
    rayHit.distance = -1;

    uint sphereCount;
    spheres.GetDimensions(sphereCount);

    for (uint i = 0; i < sphereCount; i++) {
        double3 sphereOrigin = spheres[i].origin;
        double sphereRadius = spheres[i].radius;

        double hitDistance = getClosestHitDistance(-sphereOrigin, double3(rayDirection.x, rayDirection.y, rayDirection.z), sphereRadius);
        if (hitDistance != -1 && hitDistance < closestHitDistance) {
            closestHitDistance = hitDistance;
            rayHit.distance = hitDistance;
            rayHit.sphere = spheres[i];
        }

    }

    return rayHit;
}

[numthreads(8,8,1)]
void MainEntry (uint3 id : SV_DispatchThreadID)
{
    double x = id.x / width - 0.5;
    double y = id.y / height - 0.5;

    double3 rayDirection = double3(x, y, 1);

    RayHit rayHit = getClosestRayHit(rayDirection);

    double hitDistance = rayHit.distance;

    if (hitDistance < 0) {
        Output[id.y * width + id.x] = float4(0, 0, 0, 255);
    }
    else {
        Output[id.y * width + id.x] = rayHit.sphere.color;
    }
}